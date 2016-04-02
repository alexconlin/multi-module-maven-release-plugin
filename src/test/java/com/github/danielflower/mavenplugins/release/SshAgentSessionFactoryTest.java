package com.github.danielflower.mavenplugins.release;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.util.FS;
import org.junit.Test;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;

import junit.framework.AssertionFailedError;

/**
 * @author Roland Hauser sourcepond@gmail.com
 *
 */
public class SshAgentSessionFactoryTest {
	private static final String TESTID = "testid_rsa";
	private static final String TESTID_WITH_PASSWORD = "testid_with_password_rsa";
	private final Log log = mock(Log.class);
	private final FS fs = mock(FS.class);
	
	/**
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private String getIdFile(String name) throws Exception {
		final URL url = getClass().getResource("/"+ name);
		assertNotNull(format("File {} not found", name), url);
		return new File(url.toURI()).getAbsolutePath();
	}
	
	/**
	 * @param jsch
	 * @param name
	 * @return
	 */
	private Identity getId(JSch jsch, String name) {
		final Iterator<?> it = jsch.getIdentityRepository().getIdentities().iterator();
		
		while (it.hasNext()) {
			final Identity id = (Identity)it.next();
			if (id.getName().contains(name)) {
				return id;
			}
		}
		
		throw new AssertionFailedError(format("No identity found with name %s", name));
	}
	
	/**
	 * 
	 */
	@Test
	public void createDefaultJSch() throws Exception {
		final SshAgentSessionFactory factory = new SshAgentSessionFactory(log, null, TESTID, null);
		factory.setIdentityFile(getIdFile(TESTID));
		final JSch jsch = factory.createDefaultJSch(fs);
		final Identity id = getId(jsch, TESTID);
		assertFalse(id.isEncrypted());
	}
	
	/**
	 * 
	 */
	@Test
	public void createDefaultJSch_WithPassword() throws Exception {
		final SshAgentSessionFactory factory = new SshAgentSessionFactory(log, null, TESTID_WITH_PASSWORD, null);
		factory.setIdentityFile(getIdFile(TESTID_WITH_PASSWORD));
		JSch jsch = factory.createDefaultJSch(fs);
		
		Identity id = getId(jsch, TESTID_WITH_PASSWORD);		
		assertTrue(id.isEncrypted());
		
		factory.setPassphrase("password");
		jsch = factory.createDefaultJSch(fs);
		id = getId(jsch, TESTID);
		assertFalse(id.isEncrypted());
	}
}
