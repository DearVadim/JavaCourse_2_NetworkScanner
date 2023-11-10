import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String NetworkAddress, UserAnswer;
        short HostAddress, PartAddress, HostsAvailable;
        int BeginIndex,EndIndex, TimeOut;
        boolean Errors = false, ShowErrors, UserAnswerYN;
        final Scanner sc = new Scanner(System.in);
        HostScanner[] HostScanners = new HostScanner[256];
        Thread[] HostThreads = new Thread[256];

        System.out.println("This is a Network Scanner.");
        do{
            System.out.print("Please enter an IP address in x.x.x format (Class C) to scan addresses from x.x.x.0 to x.x.x.255 (or enter 'q' to quit): ");
            UserAnswer = sc.nextLine();
            sc.reset();

            //{проверка ввода
            if(UserAnswer.equalsIgnoreCase("q"))break;

            if(UserAnswer.length()<=5){
                System.out.println("Invalid address (too short).");
                continue;
            }

            BeginIndex = 0;
            for(int i=1; i<=3; i++){

                if(i==3){
                    EndIndex = (short) UserAnswer.length();
                }else{
                    EndIndex = (short) UserAnswer.indexOf(".", BeginIndex);
                }

                if(!(EndIndex > BeginIndex)){
                    System.out.println("Invalid address (missing a point).");
                    Errors = true;
                    break;
                }
                try{
                    PartAddress = Short.parseShort(UserAnswer.substring(BeginIndex, EndIndex));
                }catch (NumberFormatException e){
                    System.out.printf("Invalid address ('%s' is not a number).\n", UserAnswer.substring(BeginIndex, EndIndex));
                    Errors = true;
                    break;
                }

                if(!(PartAddress>=0 && PartAddress<=255)){
                    System.out.printf("Invalid address ('%d' is not a valid number).\n", PartAddress);
                    Errors = true;
                    break;
                }
                BeginIndex = (short) (EndIndex + 1);
            }
            if(Errors)continue;
            //}проверка ввода

            NetworkAddress=UserAnswer;

            // ввод значения задержки
            TimeOut = InputTimeOut(1000);

            // ввод параметра отображения ошибок
            ShowErrors = InputYN("Show connection errors (y/n)? ");

            //перебор адресов, запуск потоков под проверку каждого ip-адреса
             for(HostAddress=0; HostAddress<=255; HostAddress++){
                HostScanners[HostAddress] = new HostScanner(NetworkAddress, HostAddress, TimeOut, ShowErrors);
                HostThreads[HostAddress] = new Thread(HostScanners[HostAddress], NetworkAddress+"."+HostAddress);
                HostThreads[HostAddress].start();
            }

            // ожидание завершения потоков и вывод результатов
            HostsAvailable = 0;
            for(HostAddress=0; HostAddress<=255; HostAddress++){
                try {
                    HostThreads[HostAddress].join();
                } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                }
                if(HostScanners[HostAddress].isReachable()){
                    System.out.printf("%s is reachable!\n", NetworkAddress+"."+HostAddress);
                    HostsAvailable++;
                }
            }
            System.out.printf("%d hosts is reachable\n", HostsAvailable);

            //запрос на сканирование портов
            if(HostsAvailable>0){
                do{
                    UserAnswerYN = InputYN("Perform a port scan on reachable addresses (y/n)? ");
                    if(UserAnswerYN){

                        //ввод задержки
                        TimeOut = InputTimeOut(100);

                        // ввод параметра отображения ошибок
                        ShowErrors = InputYN("Show connection errors (y/n)? ");


                        for(HostAddress=0; HostAddress<=255; HostAddress++){
                            if(HostScanners[HostAddress].isReachable()){
                                System.out.printf("Search for reachable ports for the host %s ...\n", NetworkAddress+"."+HostAddress);

                                new HostScanner(NetworkAddress, HostAddress, TimeOut, ShowErrors, true).run();

                                /* при запуске Threads активные порты не обнаруживаются, не использовать
                                PortsAvailable=0;
                                for(int i=0; i<=650; i++){
                                    BeginIndex = i*100;
                                    EndIndex = (i+1)*100-1;
                                    if(EndIndex>65535) EndIndex=65535;

                                    //перебор портов, запуск потоков под проверку каждого ip-адреса
                                    for(Port=BeginIndex; Port<=EndIndex; Port++){
                                        //PortScanners[Port] = new HostScanner(NetworkAddress, HostAddress, TimeOut, ShowErrors, Port);
                                        //PortThreads[Port] = new Thread(HostScanners[HostAddress], NetworkAddress+"."+HostAddress+":"+Port);
                                        //PortThreads[Port].start();
                                        new HostScanner(NetworkAddress, HostAddress, TimeOut, ShowErrors, Port).run();
                                        }

                                    // ожидание завершения потоков и вывод результатов
                                    for(Port=BeginIndex; Port<=EndIndex; Port++){
                                        try {
                                            PortThreads[Port].join();
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        if(PortScanners[Port].isReachable()){
                                            System.out.printf("%s is reachable!\n", NetworkAddress+"."+HostAddress+":"+Port);
                                            PortsAvailable++;
                                        }
                                    }
                                    if(i!=0 && i%100==0) {
                                        System.out.printf("Completed scanning up to port %d \n", i);
                                    }
                                }
                                //System.out.println("Completed scanning up to port 65535 \n");
                                System.out.printf("%d ports is reachable\n", PortsAvailable);*/

                                HostsAvailable--;
                                if(HostsAvailable>0){
                                    UserAnswerYN = InputYN("Continue (y/n)? ");
                                    if(!UserAnswerYN) break;
                                }
                            }
                        }
                        if(!UserAnswerYN) break;
                    } else break;
                }while (true);
            }



        }while (true);

        System.out.println("The program has finished its work.");
    }

    static boolean InputYN(String Message){
        do{
            String UserAnswer;
            final Scanner sc = new Scanner(System.in);

            System.out.print(Message);
            UserAnswer = sc.nextLine();
            sc.reset();
            if(UserAnswer.equalsIgnoreCase("y")){
                return true;
            }else if(UserAnswer.equalsIgnoreCase("n")){
                return false;
            }else System.out.println("Incorrect input");
        }while (true);
    }

    static int InputTimeOut(int RecTimeOut){
        int TimeOut;
        final Scanner sc = new Scanner(System.in);

        do{
            System.out.printf("Please Specify the timeout value in ms from 0 to 2 147 483 647 (%d ms is recommended): ", RecTimeOut);
            try {
                TimeOut = sc.nextInt();
                sc.nextLine();
                sc.reset();
            }catch (Exception e){
                TimeOut=-1;
            }

            if(TimeOut<0){
                System.out.println("Incorrect input");
            }else return TimeOut;

        }while (true);
    }
}