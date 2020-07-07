/**
* Copyright 2020 rowit Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*#########################################################################
* This is an example java application that takes 3 parameters:
* 1. The URL of the domain that is signing the files.
* 2. The path to the private key that coresponds to the URL in (1).
* 3. A path to a single file to be signed or a directory full of many files
*    to sign.
*
* Once the file(s) is/are signed it will upload the hash and signature to the
* umpint.com servers to allow users to authenticate at a later date.
*
* It will finally print out the result detailing status of the file it
* signed.
*
**/

package com.umpint.javaapi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Basis sample app showing how to access the umpint.com REST API
 * from a simple Java application.
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        System.out.println("umpint java signer starting");
        if (args.length == 0 || args.length == 1) {
            System.out.println("You must pass 3 arguments");
            System.out.println("url, path_to_private_key, path_to_file_or_directory_to_sign");
	    System.exit(1);
        }

	String url=args[0];
        File privateKeyFile=new File(args[1]);
        if (!privateKeyFile.exists()) {
            System.out.println("Could not find private key file:" + privateKeyFile.toString());
        }

        File file = new File(args[2]);
        if (!file.exists()) {
            System.out.println("Could not find file:" + file.toString());
        }

        if (file.isFile()) {
            byte[] hash = getHash(file);
            String hashString=byteArrayToHex(hash);
            RSAPrivateKey privateKey=getPrivateKeyFromFile(privateKeyFile);
            String signature=sign(privateKey,hashString);
            System.out.println(byteArrayToHex(hash));
            signature=signature.replace("\n","").replace("+","-")
                    .replace("=","_").replace("/","~")
                    .replace(" ","");
            System.out.println(signature);

            String result=getHTML("http://192.168.1.35:9000/api/v1/sign/"+url+"?hash="+hashString
                    +"&sig="+signature);
            System.out.println(result);
        } else if (file.isDirectory()){
            List<String> resultList=new ArrayList<String>();
            int i=0;
            for (final File fileEntry : file.listFiles()) {
                byte[] hash = getHash(fileEntry);
                System.out.println(i);
                i++;
                String hashString=byteArrayToHex(hash);
                RSAPrivateKey privateKey=getPrivateKeyFromFile(privateKeyFile);
                String signature=sign(privateKey,hashString);
                //System.out.println(byteArrayToHex(hash));
                signature=signature.replace("\n","").replace("+","-")
                        .replace("=","_").replace("/","~")
                        .replace(" ","");
                //System.out.println(signature);
                resultList.add("[\""+hashString+"\",\""+signature+"\"]");
            }
            Boolean first=true;
            StringBuffer sb=new StringBuffer();
            sb.append("[");
            for(String row:resultList){
                if (!first)
                    sb.append(",");
                else
                    first=false;
                sb.append(row);
            }
            sb.append("]");
            //System.out.println(sb.toString());
            String result=postHTML("http://192.168.1.35:9000/api/v1/sign/"+url+".net",sb.toString());
            System.out.println(result);
        }



    }
    public static String postHTML(String urlToRead,String data) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty( "Content-Type", "application/json" );
        conn.setRequestProperty( "Content-Length", String.valueOf(data.length()));
        OutputStream os = conn.getOutputStream();
        os.write(data.getBytes());
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));


        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }


    private static byte[] getHash(File file) throws IOException, NoSuchAlgorithmException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        byte[] md=MessageDigest.getInstance("SHA-256").digest(fileContent);
        return md;
    }

    public static RSAPrivateKey getPrivateKeyFromFile(File file) throws IOException, GeneralSecurityException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.toURI()));
        return getPrivateKeyFromString(new String(encoded,"UTF-8"));
    }

    public static RSAPrivateKey getPrivateKeyFromString(String key) throws IOException, GeneralSecurityException {
        String privateKeyPEM = key;
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("\n", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
        return privKey;
    }
    public static String sign(PrivateKey privateKey, String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(message.getBytes("UTF-8"));
        return new String(Base64.getEncoder().encode(sign.sign()), "UTF-8");
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

}

