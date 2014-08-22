package org.azavea.otm.test;

import org.azavea.otm.data.Model;
import org.azavea.otm.data.Version;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.RestClient;
import org.azavea.otm.rest.handlers.RestHandler;

import static org.mockito.Mockito.*;


public class RequestGeneratorTest extends OpenTreeMapTestCase {
    private RequestGenerator rg;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        rg = new RequestGenerator();
    }

    public void testGetVersion() {
        // Create a handler
        RestHandler<Version> handler = new RestHandler<Version>(new Version()) {
            @Override
            public void dataReceived(Version responseObject) {
                
            }

            @Override
            public void failure(Throwable e, String message) {

            }
        };
        // Create a mock RestClient
        RestClient mockClient = mock(RestClient.class);

        // Inject mock RestClient into RequestGenerator
        rg.setClient(mockClient);

        // Make the call
        rg.getVersion(handler);

        // See if mock RestClient was called correctly
        verify(mockClient).get("/version", null, handler);
    }

}
