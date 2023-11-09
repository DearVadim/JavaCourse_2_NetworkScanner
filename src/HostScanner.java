import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HostScanner implements Runnable{
    private final int TimeOut;
    private final String IPAddress;
    private boolean Reachable;
    private final boolean ShowErrors, PortsScanning;

    public boolean isReachable() {
        return Reachable;
    }

    public void setReachable(boolean reachable) {
        Reachable = reachable;
    }

    public HostScanner(String NetworkAddress, short HostAddress, int TimeOut, boolean ShowErrors){
        this.IPAddress = NetworkAddress + "." + HostAddress;
        this.TimeOut = TimeOut;
        this.ShowErrors = ShowErrors;
        this.PortsScanning = false;
    }

    public HostScanner(String NetworkAddress, short HostAddress, int TimeOut, boolean ShowErrors, boolean PortsScanning){
        this.IPAddress = NetworkAddress + "." + HostAddress;
        this.TimeOut = TimeOut;
        this.ShowErrors = ShowErrors;
        this.PortsScanning = PortsScanning;
    }

    @Override
    public void run() {
        if(!PortsScanning){
            try {
                InetAddress IAddress = InetAddress.getByName(IPAddress);
                setReachable(IAddress.isReachable(TimeOut));
            } catch (Exception e) {
                if(ShowErrors) System.out.printf("IP Address %s check error: %s\n", IPAddress, e.getMessage());
                setReachable(false);
            }
        }else{
            for(int Port=0; Port<=65535; Port++){
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(IPAddress, Port), TimeOut);
                    System.out.printf("%s is available!\n", IPAddress+":"+Port);
                } catch (Exception e) {
                    if(ShowErrors) System.out.printf("IP Address %s check error: %s\n", IPAddress+":"+Port, e.getMessage());
                }
            }

        }
    }
}
