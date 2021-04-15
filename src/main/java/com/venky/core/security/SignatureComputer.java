package com.venky.core.security;

import com.venky.core.util.ObjectUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Base64;

public class SignatureComputer {
    public static void main(String[] args) throws Exception{
        Options options = new Options();
        Option help = new Option("h","help",false, "print this message");
        Option url = new Option("u","url", true,"Url called");
        url.setRequired(true);
        Option privateKey = new Option("b64pvk","base64privatekey", true,"Private Key to Sign with");
        privateKey.setRequired(true);

        Option data = new Option("d","data", true,"Data posted to the url");
        options.addOption(help);
        options.addOption(url);
        options.addOption(privateKey);
        options.addOption(data);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options,args);
        }catch (Exception ex){
            formatter.printHelp(SignatureComputer.class.getName(),options);
            System.exit(1);
        }
        StringBuilder payload = new StringBuilder();
        String sUrl = cmd.getOptionValue("url");
        if (sUrl.startsWith("http://")){
            sUrl = sUrl.substring("http://".length());
            sUrl = sUrl.substring(sUrl.indexOf("/"));
        }
        payload.append(sUrl);
        String sData = cmd.getOptionValue("data");
        if (!ObjectUtil.isVoid(sData)){
            payload.append("|");
            payload.append(sData);
        }



        String sign = Crypt.getInstance().generateSignature(Base64.getEncoder().encodeToString(payload.toString().getBytes()),Crypt.SIGNATURE_ALGO,
                Crypt.getInstance().getPrivateKey(Crypt.KEY_ALGO,cmd.getOptionValue("base64privatekey")));
        System.out.println(sign);
    }
}
