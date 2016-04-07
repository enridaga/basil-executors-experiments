package uk.ac.open.kmi.basil.executors.it;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.basil.it.BasilTestBase;

public class QueryexecutorTest extends BasilTestBase {
	private static final Logger l = LoggerFactory.getLogger(QueryexecutorTest.class);

	@Rule
	public TestName name = new TestName();

	@Test
	public void execute() throws InterruptedException, IOException {
		l.info("#{}", name.getMethodName());
		int qn = 0;
		File rf = new File(BasilExecutorsTestSuite.reportFile);
		//rf.mkdirs();
		l.info("Reporting to: {}", rf.getAbsolutePath());
		try (FileWriter report = new FileWriter(rf)) {
			for (Entry<String, String> ee : BasilExecutorsTestSuite.apiQueryMap.entrySet()) {
				qn++;
				if (qn > BasilExecutorsTestSuite.MaxQueriesExecute) {
					l.info("Max query number reached: {}", BasilExecutorsTestSuite.MaxQueriesExecute);
					return;
				}
				String api = ee.getKey();
				String lsq = ee.getValue();
				l.info("Running api {} ({})", api, lsq);
				l.trace("Attempting to access {}", api);
				long start = System.currentTimeMillis();
				int status = 0;
				try {
					status = executor.execute(builder.buildGetRequest("/basil/" + api + "/api").withRedirects(true))
							.getResponse().getStatusLine().getStatusCode();
				} catch (UnsupportedOperationException e) {
					l.error("Failed", e);
				} catch (ClientProtocolException e) {
					l.error("Failed", e);
				} catch (IOException e) {
					l.error("Failed", e);
				} finally {
					long time = System.currentTimeMillis() - start;
					l.info("{}: {}ms - {}", lsq, time, status);
					report.write("<http://basil.kmi.open.ac.uk/experiment/");
					report.write(Integer.toString(qn));
					report.write('>');
					report.write(" <http://basil.kmi.open.ac.uk/experiment/lsq> <");
					report.write(lsq);
					report.write('>');
					report.write(" .\n <http://basil.kmi.open.ac.uk/experiment/");
					report.write(Integer.toString(qn));
					report.write('>');
					report.write(" <http://basil.kmi.open.ac.uk/experiment/status> ");
					report.write(Integer.toString(status));
					report.write(" .\n");
					report.write("<http://basil.kmi.open.ac.uk/experiment/");
					report.write(Integer.toString(qn));
					report.write('>');
					report.write(" <http://basil.kmi.open.ac.uk/experiment/ms> ");
					report.write(Long.toString(time));
					report.write(" . \n");
					Thread.sleep(BasilExecutorsTestSuite.queriesDelay);
				}
			}
		} finally{
			l.info("Reported to: {}", rf.getAbsolutePath());
		}
	}

	@Test
	public void askAnyGetJson() throws ParseException, ClientProtocolException, IOException {
		l.info("#{}", name.getMethodName());
		executor.execute(builder.buildGetRequest("/basil").withHeader("Accept", "*/*"));
		l.debug(" ... returned content length: {}", executor.getContent().length());
		executor.assertStatus(200).assertContentType("application/json").assertContentRegexp("\\[.*\\]");
	}
}
