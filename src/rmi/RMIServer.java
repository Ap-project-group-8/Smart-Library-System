package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI Server — starts the registry and binds the LibraryService implementation.
 * Run this class standalone before starting the main application.
 */
public class RMIServer {

    public static final int RMI_PORT    = 1099;
    public static final String SERVICE_NAME = "LibraryService";

    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        try {
            // Create and export the service implementation
            LibraryServiceImpl service = new LibraryServiceImpl();

            // Create (or locate) the RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind(SERVICE_NAME, service);

            System.out.println("[RMI Server] Smart Library RMI Server started on port " + RMI_PORT);
            System.out.println("[RMI Server] Service bound as: " + SERVICE_NAME);
        } catch (Exception e) {
            System.err.println("[RMI Server] Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
