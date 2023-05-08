package edu.uw.info314.xmlrpc.server;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.logging.*;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static spark.Spark.*;

class Call {
    public String name;
    public List<Object> args = new ArrayList<Object>();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());
    private static final int PORT = 8080;

    public static void main(String[] args) {
        port(PORT);
        LOG.info("Starting up on port " + PORT);

        before((req, res) -> {
            if (!req.uri().equals("/RPC")) {
                halt(404, "URL must be /RPC");
            }
            if (!req.requestMethod().equals("POST")) {
                halt(405, "Only POST is supported");
            }
        });

        // This is the mapping for POST requests to "/RPC";
        // this is where you will want to handle incoming XML-RPC requests
        post("/RPC", (req, res) -> {
            Call call = extractXMLRPCCall(req.body());
            String methodName = call.name;

            if(!req.body().contains(methodName) || !req.body().contains("<i4>")) {
                return createFaultXMLResponse(3, "illegal argument type");
            }

            int[] params = new int[call.args.size()];
            for (int i = 0; i < params.length; i++) {
                params[i] = (int)call.args.get(i);
            }

            String result;
            // handle cases
            Calc c = new Calc();
            switch (methodName) {
                case "add":
                    try {
                        result = createXMLResponse(c.add(params));
                    } catch (ArithmeticException e) {
                        result = createFaultXMLResponse(2, "overflow");
                    }
                    break;
                case "subtract":
                    result = createXMLResponse(c.subtract(params[0], params[1]));
                    break;
                case "multiply":
                    try {
                        result = createXMLResponse(c.multiply(params));
                    } catch (ArithmeticException e) {
                        result = createFaultXMLResponse(2, "overflow");
                    }
                    break;
                case "divide":
                    try {
                        result = createXMLResponse(c.divide(params[0], params[1]));
                    } catch (ArithmeticException e) {
                        result = createFaultXMLResponse(1, "divide by zero");
                    }
                    break;
                case "modulo":
                    try {
                        result = createXMLResponse(c.modulo(params[0], params[1]));
                    } catch (ArithmeticException e) {
                        result = createFaultXMLResponse(1, "divide by zero");
                    }
                    break;
                default:
                    result = "something went wrong";
                    break;
            }

            res.status(200);
            return result;
        });
    }

    public static String createXMLResponse(int r) {
        String result =
                "<?xml version=\"1.0\"?>\n" +
                "<methodResponse>\n" +
                "  <params>\n" +
                "    <param>\n" +
                "        <value><string>" + r + "</string></value>\n" +
                "    </param>\n" +
                "  </params>\n" +
                "</methodResponse>";
        return result;
    }

    public static String createFaultXMLResponse(int fc, String fs) {
        String result =
                "<?xml version=\"1.0\"?>\n" +
                "<methodResponse>\n" +
                "  <fault>\n" +
                "    <value>\n" +
                "      <struct>\n" +
                "        <member>\n" +
                "          <name>faultCode</name>\n" +
                "          <value><int>"+ fc +"</int></value>\n" +
                "        </member>\n" +
                "        <member>\n" +
                "          <name>faultString</name>\n" +
                "          <value><string>"+ fs +"</string></value>\n" +
                "        </member>\n" +
                "      </struct>\n" +
                "    </value>\n" +
                "  </fault>\n" +
                "</methodResponse>";

        return result;
    }

    public static Call extractXMLRPCCall(String xml) {
        Call result = new Call();
        // parse XML
        try {
            // parse XML string
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(bais);
            doc.getDocumentElement().normalize();



            // get methodName
            result.name = doc.getElementsByTagName("methodName").item(0).getTextContent();

            // get params
            NodeList list = doc.getElementsByTagName("i4");
            List<Object> arguments = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                arguments.add(Integer.valueOf(list.item(i).getTextContent()));
            }

            result.args = arguments;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

}
