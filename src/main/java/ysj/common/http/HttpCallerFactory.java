package ysj.common.http;


import java.io.File;

public class HttpCallerFactory {
    public static HttpCaller create(){
        return new HttpCallerClient();
    }

    public static HttpCaller create(String principle, File keytab){
        if(principle==null || principle.length()==0 || keytab.exists()){
            return create();
        }else{
            return new KerberosHttpCallerClient(principle,keytab);
        }
    }
}
