package uk.ac.open.kmi.basil.executors.it;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import uk.ac.open.kmi.basil.it.BasilTestServer;

@RunWith(Suite.class)
@Suite.SuiteClasses({QueryloaderTest.class, QueryexecutorTest.class })
public class BasilExecutorsTestSuite {
	private static final Logger l = LoggerFactory.getLogger(BasilExecutorsTestSuite.class);
	private static final String queriesFile = System.getProperty("test.queriesFile");
	public static final String reportFile = System.getProperty("test.queries.reportFile");
	public static final int queriesDelay = Integer.parseInt(System.getProperty("test.queries.delay"));
	static final Model queriesData = ModelFactory.createDefaultModel();
	static final Map<String,String> apiQueryMap = new HashMap<String,String>();
	static final Property spinText = queriesData.getProperty("http://spinrdf.org/sp#text");
	static final Property lsqvEndpoint = queriesData.getProperty("http://lsq.aksw.org/vocab#endpoint");
	
	public static final int MaxQueriesExecute = Integer.parseInt(System.getProperty("test.queries.max"));

	private static void loadQueries() throws FileNotFoundException{
		l.info("Queries file is {} ,{}", queriesFile, new File(queriesFile).exists());
		InputStream is = new FileInputStream(queriesFile);
		queriesData.read(is, null, "N-TRIPLES");
		l.info("{} queries to be loaded",
				queriesData.listStatements(null, queriesData.getProperty("http://spinrdf.org/sp#text"), (RDFNode) null).toSet().size());
		
	}
	
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		loadQueries();
		BasilTestServer.start();
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		BasilTestServer.destroy();
	}
}
