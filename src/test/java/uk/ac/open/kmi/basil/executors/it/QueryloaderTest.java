package uk.ac.open.kmi.basil.executors.it;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import uk.ac.open.kmi.basil.it.AuthenticatedTestBase;
import uk.ac.open.kmi.basil.it.BasilTestServer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryloaderTest extends AuthenticatedTestBase {
	private static final Logger l = LoggerFactory.getLogger(QueryloaderTest.class);

	@Rule
	public TestName name = new TestName();

	private String createApi(String query, String endpoint) throws ClientProtocolException, IOException {
		HttpPut put = new HttpPut(BasilTestServer.getServerBaseUrl() + "/basil");
		put.addHeader("X-Basil-Endpoint", endpoint);
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(IOUtils.toInputStream(query));
		put.setEntity(entity);
		HttpResponse response = executor.execute(builder.buildOtherRequest(put)).assertStatus(201).getResponse();
		l.debug(" > Response headers:");
		for (Header h : response.getAllHeaders()) {
			l.debug(" >> {}: {}", h.getName(), h.getValue());
		}
		String loc = response.getFirstHeader("Location").getValue();
		String createdId = loc.substring(loc.lastIndexOf('/') + 1);
		l.info("Api {} created", createdId);
		return createdId;
	}

	@Test
	public void askAnyGetJson() throws ParseException, ClientProtocolException, IOException {
		l.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "*/*"));
		l.debug(" ... returned content: {}", executor.getContent());
		executor.assertStatus(200).assertContentType("application/json").assertContentRegexp("\\[.*\\]");
	}

	@Test
	public void populateApis() throws Exception {
		l.info("#populateApis()");
		ResIterator it = BasilExecutorsTestSuite.queriesData
				.listResourcesWithProperty(BasilExecutorsTestSuite.spinText);
		String defaultEndpoint = "http://dbpedia.org/sparql";
		int qn = 0;
		while (it.hasNext()) {
			qn++;
			if (qn > BasilExecutorsTestSuite.MaxQueriesExecute) {
				l.info("Max query number reached: {}", BasilExecutorsTestSuite.MaxQueriesExecute);
				return;
			}
			Resource query = it.next();
			String queryt = query.getProperty(BasilExecutorsTestSuite.spinText).getObject().asLiteral()
					.getLexicalForm();
			String endpoint = defaultEndpoint;
			if (query.hasProperty(BasilExecutorsTestSuite.lsqvEndpoint)) {
				endpoint = query.getProperty(BasilExecutorsTestSuite.lsqvEndpoint).getObject().asLiteral()
						.getLexicalForm();
			}
			String id = createApi(queryt, endpoint);
			BasilExecutorsTestSuite.apiQueryMap.put(id, query.getURI());
			Thread.sleep(50);
		}
	}
}
