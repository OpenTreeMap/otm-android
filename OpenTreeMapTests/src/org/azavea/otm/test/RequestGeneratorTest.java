package org.azavea.otm.test;

import org.azavea.otm.data.Version;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.RestClient;
import org.azavea.otm.rest.handlers.RestHandler;

//import com.google.android.testing.mocking.AndroidMock;
//import com.google.android.testing.mocking.UsesMocks;
import com.loopj.android.http.JsonHttpResponseHandler;
import static org.mockito.Mockito.*;

import junit.framework.TestCase;

public class RequestGeneratorTest extends TestCase {
	private RequestGenerator rg;
	
	protected void setUp() throws Exception {
		super.setUp();
		rg = new RequestGenerator();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetVersion() {
		// Create a handler
		RestHandler<Version> handler = new RestHandler<Version>(new Version());
		// Create a mock RestClient
		RestClient mockClient = mock(RestClient.class);

		// Inject mock RestClient into RequestGenerator
		rg.setClient(mockClient);

		// Make the call
		rg.getVersion(handler);

		// See if mock RestClient was called
		// correctly
		verify(mockClient).get("/version", null, handler);
	}
		
}
