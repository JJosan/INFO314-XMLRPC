import java.io.*;
import java.lang.Runtime.Version;
import java.net.*;
import java.net.http.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import org.w3c.dom.Node;

/**
 * This approach uses the java.net.http.HttpClient classes, which
 * were introduced in Java11.
 */
public class Client {
    private static String host;
    private static int port;

    public static void main(String... args) throws Exception {
        // check for args
        if (args.length != 2) {
            System.out.println("please provide arguments");
            return;
        }

        host = args[0];
        port = Integer.valueOf(args[1]);

        System.out.println(add() == 0);
        System.out.println(add(1, 2, 3, 4, 5) == 15);
        System.out.println(add(2, 4) == 6);
        System.out.println(subtract(12, 6) == 6);
        System.out.println(multiply(3, 4) == 12);
        System.out.println(multiply(1, 2, 3, 4, 5) == 120);
        System.out.println(divide(10, 5) == 2);
        System.out.println(modulo(10, 5) == 0);
    }
    public static int add(int lhs, int rhs) throws Exception {
        return sendRequest("add", new Object[] {lhs, rhs});
    }
    public static int add(Integer... params) throws Exception {
        return sendRequest("add", (Object[])params);
    }
    public static int subtract(int lhs, int rhs) throws Exception {
        return sendRequest("subtract", new Object[] {lhs, rhs});
    }
    public static int multiply(int lhs, int rhs) throws Exception {
        return sendRequest("multiply", new Object[] {lhs, rhs});
    }
    public static int multiply(Integer... params) throws Exception {
        return sendRequest("multiply", (Object[])params);
    }
    public static int divide(int lhs, int rhs) throws Exception {
        return sendRequest("divide", new Object[] {lhs, rhs});
    }
    public static int modulo(int lhs, int rhs) throws Exception {
        return sendRequest("modulo", new Object[] {lhs, rhs});
    }

    public static int sendRequest(String methodName, Object... arguments) {
        try {
            // Create instance of client
            HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

            // Create a request body
            String parameters = "<params>";
            if (arguments.length == 0) {
                parameters += "<param><value><i4>" + 0 + "</i4></value></param>";
            } else {
                for (Object param : arguments) {
                    parameters += "<param><value><i4>" + (Integer) param + "</i4></value></param>";
                }
            }
            parameters += "</params>";

            String requestBody = "<?xml version = '1.0'?><methodCall><methodName>" + methodName + "</methodName>" + parameters + "</methodCall>";

            //  Send request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+host+":"+port+"/RPC"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "text/xml")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Object[] result = parseResponse(response.body());
            if (result.length == 1) {
                return Integer.valueOf((String)result[0]);
            } else {
                throw new ArithmeticException(result[0] + ", " + result[1]);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
        
    }

    public static Object[] parseResponse(String body) {
        Object[] result;
        // parse XML
        try {
            // parse XML string
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes());
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(bais);
            doc.getDocumentElement().normalize();

            // check if result is valid
            if (doc.getDocumentElement().getElementsByTagName("fault").getLength() == 0) {
                result = new Object[1];
                result[0] = doc.getElementsByTagName("string").item(0).getTextContent();
                return result;
            } else {
                result = new Object[2];
                String fc = doc.getElementsByTagName("int").item(0).getTextContent();
                String fs = doc.getElementsByTagName("string").item(0).getTextContent();
                result[0] = fc;
                result[1] = fs;
                return result;
            }    
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
