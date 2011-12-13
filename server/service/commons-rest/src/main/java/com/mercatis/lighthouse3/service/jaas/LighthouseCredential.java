package com.mercatis.lighthouse3.service.jaas;

import java.security.MessageDigest;

import org.eclipse.jetty.http.security.Credential;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;

import com.mercatis.lighthouse3.service.jaas.util.Base64;

public class LighthouseCredential extends Credential {

	private static final long serialVersionUID = 2985629948366876738L;
	private static final Object lock = new Object();
	private static MessageDigest digest;
	
	private String sha1;
	
	public LighthouseCredential(String sha1) {
		this.sha1 = sha1;
	}

    public static String hash(String password)
    {
        try
        {
            byte[] data;
            synchronized (lock)
            {
                if (digest == null)
                {
                    try
                    {
                        digest = MessageDigest.getInstance("SHA1");
                    }
                    catch (Exception e)
                    {
                        Log.warn(e);
                        return null;
                    }
                }

                digest.reset();
                digest.update(password.getBytes(StringUtil.__ISO_8859_1));
                data = digest.digest();
            }
            return Base64.encodeBytes(data);
        }
        catch (Exception e)
        {
            Log.warn(e);
            return null;
        }
    }
	
	@Override
	public boolean check(Object credentials) {
		if (credentials instanceof String) {
			String encoded = hash((String) credentials);
			return encoded.equals(sha1);
		}
		return false;
	}

}
