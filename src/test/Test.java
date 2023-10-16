/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package test;

import Door.Access.Command.CommandDetail;
import Door.Access.Command.CommandParameter;
import Door.Access.Command.INCommand;
import Door.Access.Command.INCommandResult;
import Door.Access.Connector.ConnectorAllocator;
import Door.Access.Connector.ConnectorDetail;
import Door.Access.Connector.E_ControllerType;
import Door.Access.Connector.INConnectorEvent;
import Door.Access.Connector.TCPClient.TCPClientDetail;
import Door.Access.Data.AbstractTransaction;
import Door.Access.Data.INData;
import Door.Access.Door8800.Command.Data.CardTransaction;
import Door.Access.Door8800.Command.Data.Door8800WatchTransaction;
import Door.Access.Door8800.Command.Data.E_WeekDay;
import Door.Access.Door8800.Command.Data.TCPDetail;
import Door.Access.Door8800.Command.Data.TimeGroup.DayTimeGroup_ReaderWork;
import Door.Access.Door8800.Command.Data.TimeGroup.TimeSegment_ReaderWork;
import Door.Access.Door8800.Command.Data.TimeGroup.TimeSegment_ReaderWork.ReaderWorkType;
import Door.Access.Door8800.Command.Door.OpenDoor;
import Door.Access.Door8800.Command.Door.Parameter.OpenDoor_Parameter;
import Door.Access.Door8800.Command.Door.ReadReaderWorkSetting;
import Door.Access.Door8800.Command.Door.Result.ReadReaderWorkSetting_Result;
import Door.Access.Door8800.Command.System.BeginWatch;
import Door.Access.Door8800.Command.System.Parameter.WriteKeepAliveInterval_Parameter;
import Door.Access.Door8800.Command.System.Parameter.WriteTCPSetting_Parameter;
import Door.Access.Door8800.Command.System.ReadTCPSetting;
import Door.Access.Door8800.Command.System.WriteKeepAliveInterval;
import Door.Access.Door8800.Command.System.WriteTCPSetting;
import Door.Access.Door8800.Door8800Identity;
import java.util.concurrent.Semaphore;
import static javax.management.Query.times;
//for API
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;



/**
 *
 * @author USER
 */
public class Test implements INConnectorEvent {
    private ConnectorAllocator _Allocator;
     private String LocalIP;
    private int LocalPort;
     private final Semaphore available = new Semaphore(0, true);
    public Test() {
	  	//在构造方法中获取实例（单例）
        _Allocator = ConnectorAllocator.GetAllocator();
        //添加事件通知
        _Allocator.AddListener(this);
        try {
            String LocalIP = "1.0.0.67";
            int LocalPort = 8000;
            _Allocator.Listen(LocalIP, LocalPort);
            System.out.println("Listening...");
            readTCPSetting();
        } catch (Exception e) {
            // Handle the exception
        }
          //readTCPSetting();
    }
     public void syn() {
        try {
            available.acquire();
        } catch (Exception e) {
        }

    }
      public void readTCPSetting() {

        CommandParameter parameter = new CommandParameter(getCommandDetail());
        ReadTCPSetting cmd = new ReadTCPSetting(parameter);
        _Allocator.AddCommand(cmd);
        System.out.println("Start reading device TCP parameters");

    }
     
    
    
     public CommandDetail getCommandDetail() {
        TCPClientDetail tcpClient = new TCPClientDetail("1.0.0.67", 8000);
        tcpClient.Timeout = 5000;//连接超时时间（毫秒）
        tcpClient.RestartCount = 0;//重新连接次数		
        Door8800Identity idt = new Door8800Identity("MC-5924T23010053", "FFFFFFFF", E_ControllerType.Door8900);
        CommandDetail commandDetail = new CommandDetail();
        commandDetail.Connector = tcpClient;
        commandDetail.Identity = idt;
        return  commandDetail;
    }
     
      public void openDoor() {
       CommandDetail commandDetail = getCommandDetail();//Get Command Detail Object
       OpenDoor_Parameter parameter = new OpenDoor_Parameter(commandDetail); //声明远程开门命令参数对象
        //设置开门参数 1-4 是门号，1是开门 0是不开门
        parameter.Door.SetDoor(1, 1);
        OpenDoor cmd = new OpenDoor(parameter);
         //Add Command to Communication Connector Queue
        _Allocator.AddCommand(cmd);
    }
      
      
        @Override
    public void CommandCompleteEvent(INCommand cmd, INCommandResult result) {
        
        if(cmd instanceof ReadReaderWorkSetting)
        {
            ReadReaderWorkSetting_Result ret = (ReadReaderWorkSetting_Result) result;
            System.out.println(ret.DoorNum+"号门 星期一认证方式");
            DayTimeGroup_ReaderWork day1 = ret.ReaderWork.GetItem(E_WeekDay.Monday);//获取星期一的认证方式
            TimeSegment_ReaderWork time1 = day1.GetItem(1);//1-8时段
            short[] beginTime = new  short[2];
            time1.GetBeginTime(beginTime);
            short[] endTime = new  short[2];
            time1.GetEndTime(endTime);
            System.out.println("时段1开始时间："+beginTime[0]+":"+beginTime[1]);
            System.out.println("时段1结束时间："+endTime[0]+":"+endTime[1]);
            System.out.println("仅读卡："+time1.GetWorkType(ReaderWorkType.OnlyCard));
            System.out.println("仅密码："+time1.GetWorkType(ReaderWorkType.OnlyPassword));
            System.out.println("读卡加密码："+time1.GetWorkType(ReaderWorkType.CardAndPassword));
            System.out.println("手动输入卡号+密码："+time1.GetWorkType(ReaderWorkType.InputCardAndPassword));
        }
  
           // beginWatch();
//          if (result instanceof ReadTCPSetting_Result) {
//            System.out.println("Read device TCP parameters successfully");
//            ReadTCPSetting_Result tcpResult = (ReadTCPSetting_Result) result;
//            writeTCPSetting(tcpResult.TCP);
//        }
//           if (cmd instanceof WriteTCPSetting) {
//            System.out.println("Writing device TCP parameters successfully");
//            
//        }
//           
//         if (cmd instanceof BeginWatch) {
//            System.out.println("Device monitoring enabled successfully");
//       
//            writeKeepAliveInterval();
//        }
//          if (cmd instanceof WriteKeepAliveInterval) {
//            System.out.println("Write the keep-alive interval successfully");
//             System.out.println("End of setup process");
//            System.out.println("Wait for device to connect");
//        }
         if (cmd instanceof OpenDoor) {
              System.out.println("Door Opened");
         }
    }
    
      private void beginWatch() {
        BeginWatch cmd = new BeginWatch(new CommandParameter((getCommandDetail())));
        _Allocator.AddCommand(cmd);

    }
      
      private void writeKeepAliveInterval() {
        WriteKeepAliveInterval_Parameter par = new WriteKeepAliveInterval_Parameter(getCommandDetail());
        par.IntervalTime = 30;//取值范围：0-3600,0--关闭功能 
        WriteKeepAliveInterval cmd = new WriteKeepAliveInterval(par);
        _Allocator.AddCommand(cmd);
    }
    
     private void writeTCPSetting(TCPDetail detail) {
        detail.SetServerAddr(LocalIP);
        detail.SetServerIP(LocalIP);
        detail.SetServerPort(LocalPort);
        WriteTCPSetting_Parameter parameter = new WriteTCPSetting_Parameter(getCommandDetail(), detail);
        WriteTCPSetting cmd = new WriteTCPSetting(parameter);
        _Allocator.AddCommand(cmd);
        System.out.println("Start writing device TCP parameters");
    }
     
     @Override
    public void CommandProcessEvent(INCommand cmd) {
    	 //System.out.println("current command:"+cmd.getClass().toString()+",Current progress:"+cmd.getProcessStep()+"/"+cmd.getProcessMax() );
        //当前命令:OpenDoor,当前进度:1/1
         beginWatch();
    }
    
      @Override
    public void ConnectorErrorEvent(INCommand cmd, boolean isStop) {
        String sCmd=cmd.getClass().toString();
         if (isStop) {
                System.out.println(sCmd+"命令已手动停止!");
            } else {
                System.out.println(sCmd+"网络连接失败!");
            }
        
    }
    
      @Override
    public void ConnectorErrorEvent(ConnectorDetail detial) {
        try {         
           System.out.println("Network channel failure:");
        } catch (Exception e) {
            System.out.println("doorAccessiodemo.frmMain.ConnectorErrorEvent() -- " + e.toString());
        }
    }
    
     @Override
    public void CommandTimeout(INCommand cmd) {          
        System.out.println("命令超时:"+cmd.getClass().toString());
    }
    
      @Override
    public void PasswordErrorEvent(INCommand cmd) {
         System.out.println("通讯密码错误，已失败！");
    }
    
     @Override
    public void ChecksumErrorEvent(INCommand cmd) {
         System.out.println("命令返回的校验和错误，已失败！");
    }
    
      @Override
    public void WatchEvent(ConnectorDetail detial, INData event) {
        
        try {
            Door8800WatchTransaction watchEvent = (Door8800WatchTransaction) event;
                  AbstractTransaction tr = (AbstractTransaction) watchEvent.EventData;
                  CardTransaction card = (CardTransaction) watchEvent.EventData;
                   boolean found = false;
                   String cardFound = "";
            //API FETCHING
              // try {
            // 1. Create a URL object
                URL url = new URL("http://165.232.165.96/api/fetch-cards");

                // 2. Open a connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 3. Set request method (GET)
                connection.setRequestMethod("GET");

                // 4. Read the response
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) { // HTTP status code 200 indicates success
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // 5. Process the response
                    String responseData = response.toString();
                    
                    String[] cardNumbers;
                     String[] responseParts = responseData.split("\"card_number\":\"");
                    cardNumbers = new String[responseParts.length - 1];
                    
                      for (int i = 1; i < responseParts.length; i++) {
                        String cardNumber = responseParts[i].split("\"")[0];
                        cardNumbers[i - 1] = cardNumber;
                    }
                      
                       // Check if the target card number exists in the array
                   
                    for (String cardNumber : cardNumbers) {
                        if (cardNumber.equals(card.CardData)) {
                            found = true;
                            cardFound = cardNumber;
                            break;
                        }
                    }
                    
                     if (found) {
                        System.out.println("Card number " + card.CardData + " exists in the API response.");
                    } else {
                        System.out.println("Card number " + card.CardData + " does not exist in the API response.");
                    }
//                    System.out.println("API Response: " + responseData);
                } else {
                    System.out.println("API Request failed with response code: " + responseCode);
                }

                connection.disconnect();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
             
                   if (found) { 
                       
                       //POST DATA
                       URL urlPost = new URL("http://165.232.165.96/api/create-card");
                        HttpURLConnection connectionPost = (HttpURLConnection) urlPost.openConnection();
                       connectionPost.setRequestMethod("POST");
                        connectionPost.setRequestProperty("Content-Type", "application/json");
                       Map<String, String> requestData = new HashMap<>();
                        requestData.put("card_number", cardFound);
                         String jsonInputString = mapToJson(requestData);
                       // Enable input and output streams
                        connectionPost.setDoOutput(true);
                        // Write the JSON payload to the output stream
                        try (OutputStream os = connectionPost.getOutputStream();
                             OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8")) {
                            osw.write(jsonInputString);
                            osw.flush();
                            osw.close();
                        }
            
                          int responseCodePost = connectionPost.getResponseCode();
                           if (responseCodePost == HttpURLConnection.HTTP_OK) {
                            // Read and print the response
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connectionPost.getInputStream()))) {
                                String line;
                                StringBuilder response = new StringBuilder();
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                System.out.println("API Response: " + response.toString());
                            }
                             CommandDetail commandDetail = getCommandDetail();//Get Command Detail Object
                            OpenDoor_Parameter parameter = new OpenDoor_Parameter(commandDetail); //声明远程开门命令参数对象
                             //设置开门参数 1-4 是门号，1是开门 0是不开门
                             parameter.Door.SetDoor(card.DoorNum(), 1);
                             OpenDoor cmd = new OpenDoor(parameter);
                              //Add Command to Communication Connector Queue
                             _Allocator.AddCommand(cmd);
                        } else {
                            System.out.println("API Request failed with response code: " + responseCodePost);
                        }

                        // Close the connection
                        connectionPost.disconnect();
                       
                      
                 
                   }else{
                       System.out.print("You are not allowed");
                   }
            /*
            包含 出入记录、alarm记录、软件开门消息、门磁消息等
            */
//             System.out.println("Event");
//            System.out.print(event.toString());
//            StringBuilder strBuf = new StringBuilder(100);
//            strBuf.append("Data monitoring:");
//            if (event instanceof Door.Access.Door89H.Command.Data.CardTransaction) {
//                  Door8800WatchTransaction watchEvent = (Door8800WatchTransaction) event;
//                  AbstractTransaction tr = (AbstractTransaction) watchEvent.EventData;
//                  CardTransaction card = (CardTransaction) watchEvent.EventData;
//                   if ("12339606".equals(card.CardData)) { 
//                       System.out.print(card.CardData);
//                 // Test test = new Test();
//        
//                            //test.openDoor();
//                   }
////                Door8800WatchTransaction WatchTransaction = (Door8800WatchTransaction) event;
////                strBuf.append("，SN：");
////                strBuf.append(WatchTransaction.SN);
////                strBuf.append("\n");
//            } else {
//                strBuf.append("，Unknown Event：");
//                strBuf.append(event.getClass().getName());
//            }
           // System.out.println(strBuf);
        } catch (Exception e) {
            System.out.println("doorAccessiodemo.frmMain.WatchEvent() -- " + e.toString());
        }
    }
    
     // Convert a Map to a JSON string
    private static String mapToJson(Map<String, String> data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        if (!data.isEmpty()) {
            json.deleteCharAt(json.length() - 1); // Remove the trailing comma
        }
        json.append("}");
        return json.toString();
    }
       
      @Override
    public void ClientOnline(ConnectorDetail client) {
		//需要将ConnectorDetail 类转为TCPClientDetail或者UDPDetail 具体子类对象
    }
    
     @Override
    public void ClientOffline(ConnectorDetail client) {
        //需要将ConnectorDetail 类转为TCPClientDetail或者UDPDetail 具体子类对象
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       Test test = new Test();
         test.syn();
         //test.openDoor();
    }
    
}
