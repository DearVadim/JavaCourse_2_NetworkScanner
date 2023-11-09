import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String NetworkAddress, UserAnswer;
        short HostAddress, PartAddress, BeginIndex,EndIndex;
        int TimeOut;
        boolean Errors = false, AddressesAvailable, ShowErrors;
        final Scanner sc = new Scanner(System.in);
        //List<HostScanner>[] HostScanners = new List[256];
        HostScanner[] HostScanners = new HostScanner[256];
        Thread[] HostThreads = new Thread[256];

        System.out.println("This is a Network Scanner.");
        do{
            System.out.print("Please enter an IP address in x.x.x format (Class C) to scan addresses from x.x.x.0 to x.x.x.255 (or enter 'q' to quit): ");
            UserAnswer = sc.nextLine();

            //{проверка ввода
            if(UserAnswer.equalsIgnoreCase("q"))break;

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
                    PartAddress = Short.valueOf(UserAnswer.substring(BeginIndex, EndIndex));
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
            TimeOut = InputTimeOut(5000);

            // ввод параметра отображения ошибок
            ShowErrors = InputShowErrors();

            //перебор адресов, запуск потоков под проверку каждого ip-адреса
             for(HostAddress=0; HostAddress<=255; HostAddress++){
                HostScanners[HostAddress] = new HostScanner(NetworkAddress, HostAddress, TimeOut, ShowErrors);
                HostThreads[HostAddress] = new Thread(HostScanners[HostAddress], NetworkAddress+"."+HostAddress);
                HostThreads[HostAddress].start();
            }

            // ожидание завершения потоков
            for(HostAddress=0; HostAddress<=255; HostAddress++){
                try {
                    HostThreads[HostAddress].join();
                } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                }
            }

            // вывод результатов
            AddressesAvailable = false;
            for(HostAddress=0; HostAddress<=255; HostAddress++){
                if(HostScanners[HostAddress].isReachable()){
                    System.out.printf("%s is reachable!\n", NetworkAddress+"."+HostAddress);
                    AddressesAvailable = true;
                }
            }

            //запрос на сканирование портов
            if(AddressesAvailable){
                do{
                    System.out.print("Perform a port scan on available addresses (y/n)? ");
                    UserAnswer = sc.nextLine();
                    if(UserAnswer.equalsIgnoreCase("y")){

                        //ввод задержки
                        TimeOut = InputTimeOut(1000);

                        // ввод параметра отображения ошибок
                        ShowErrors = InputShowErrors();

                        for(HostAddress=0; HostAddress<=255; HostAddress++){
                            if(HostScanners[HostAddress].isReachable()){
                                System.out.printf("Search for available ports for the host %s ...\n", NetworkAddress+"."+HostAddress);
                                HostScanners[HostAddress] = new HostScanner(NetworkAddress, HostAddress, TimeOut, ShowErrors, true);
                                HostScanners[HostAddress].run();
                            }
                        }

                    } else if (UserAnswer.equalsIgnoreCase("n")) {
                        break;
                    }else{
                        System.out.println("Incorrect input");
                    }
                }while (true);
            }



        }while (true);

        System.out.println("The program has finished its work.");
    }

    static boolean InputShowErrors(){
        do{
            String UserAnswer;
            final Scanner sc = new Scanner(System.in);

            System.out.print("Show connection errors (y/n)? ");
            UserAnswer = sc.nextLine();
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
            }catch (Exception e){
                TimeOut=-1;
            }

            if(TimeOut<0){
                System.out.println("Incorrect input");
            }else return TimeOut;

        }while (true);
    }
}