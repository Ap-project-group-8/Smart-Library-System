package rmi;

import model.Book;
import model.Loan;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * RMI Client — connects to the RMI server and calls remote methods.
 * Used by the GUI to perform remote queries.
 */
public class RMIClient {

    private LibraryService service;
    private boolean connected = false;

    /**
     * Attempts to connect to the RMI server.
     * @return true if connection succeeded
     */
    public boolean connect() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", RMIServer.RMI_PORT);
            service = (LibraryService) registry.lookup(RMIServer.SERVICE_NAME);
            connected = true;
            System.out.println("[RMI Client] Connected to RMI server.");
            return true;
        } catch (Exception e) {
            connected = false;
            System.err.println("[RMI Client] Could not connect: " + e.getMessage());
            return false;
        }
    }

    public boolean isConnected() { return connected; }

    public List<Book> searchBook(String title) {
        if (!connected) return List.of();
        try {
            return service.searchBook(title);
        } catch (Exception e) {
            System.err.println("[RMI Client] searchBook error: " + e.getMessage());
            return List.of();
        }
    }

    public int checkAvailability(int bookId) {
        if (!connected) return -1;
        try {
            return service.checkAvailability(bookId);
        } catch (Exception e) {
            return -1;
        }
    }

    public List<Loan> getMemberLoans(int memberId) {
        if (!connected) return List.of();
        try {
            return service.getMemberLoans(memberId);
        } catch (Exception e) {
            return List.of();
        }
    }

    public String ping() {
        if (!connected) return "Not connected";
        try {
            return service.ping();
        } catch (Exception e) {
            connected = false;
            return "Connection lost: " + e.getMessage();
        }
    }
}
