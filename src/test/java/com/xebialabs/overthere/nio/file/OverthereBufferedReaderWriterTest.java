package com.xebialabs.overthere.nio.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;

public class OverthereBufferedReaderWriterTest extends BaseTest {

	OverthereFileSystem fs;
	private final String username = "vagrant";
	private final String password = "vagrant";
	private final String host = "127.0.0.1";
	private final int port = 2222;
	private final String os = "UNIX";
		
	@BeforeClass
	public void createFileSystems() throws IOException {	
		for (OverthereProtocol protocol : OverthereProtocol.testableValuesList()) {
			FileSystems.newFileSystem(getURI(protocol), getMap(protocol));
		}
	}
	
	@AfterClass
	public void cleanUpFileSystems() throws IOException {
		for (OverthereProtocol protocol : OverthereProtocol.testableValuesList()) {
			FileSystems.getFileSystem(getURI(protocol)).close();
		}
	}
	
	@BeforeMethod
	public void init() throws IOException {
		fs = (OverthereFileSystem) FileSystems.getFileSystem(getURI(OverthereProtocol.SSH_SCP));
	}
		
	private URI getURI(OverthereProtocol protocol) {
		return URI.create(protocol.forUri() + "://" + username + "@" + host + ":" + port + "/?os=" + os);
	}
	
	private Map<String, Object> getMap(OverthereProtocol protocol) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("password", password);
		
		if (protocol.forUri().contains("sudo")) {
			map.put(SshConnectionBuilder.SUDO_USERNAME, "root");
		}
		
		return map;
	}

	
	@Test(dataProvider = "protocol")
	public void testBufferedWriter(OverthereProtocol protocol) throws IOException {
		
		fs = (OverthereFileSystem) FileSystems.getFileSystem(getURI(protocol));
		
		fs.getConnection().execute(null, CmdLine.build("touch", "test.txt"));
		
		String fileName = "test" + protocol + ".txt";
		
		try (BufferedWriter writer = Files.newBufferedWriter(fs.getPath(fileName), StandardCharsets.UTF_8)) {
			writer.write("this has passed");
		};
		
		Output output = new Output();
				
		Assert.assertEquals(fs.getConnection().execute(output, CmdLine.build("cat", fileName)), 0, "return code");
		Assert.assertEquals(output.getStdOut(), "this has passed", "std output");
	
	}
	
	@Test(dependsOnMethods = { "testBufferedWriter" }, dataProvider = "protocol")
	public void testBufferedReader(OverthereProtocol protocol) throws IOException {
		
		fs = (OverthereFileSystem) FileSystems.getFileSystem(getURI(protocol));
		
		try (BufferedReader reader = Files.newBufferedReader(fs.getPath("test" + protocol + ".txt"), StandardCharsets.UTF_8)) {
			Assert.assertEquals(reader.readLine(), "this has passed");
		}
		
	}
	
	@Test(dependsOnMethods = { "testBufferedWriter" }, dataProvider = "protocol")
	public void testStuff(OverthereProtocol protocol) throws IOException {
		fs = (OverthereFileSystem) FileSystems.getFileSystem(getURI(protocol));
		
		System.out.println(Files.size(fs.getPath("test" + protocol + ".txt")));
		
		InputStream stream = Files.newInputStream(fs.getPath("test.txt"));
	
		System.out.println(stream.skip(Long.MAX_VALUE));
		
	}
	
	public class Output implements OverthereProcessOutputHandler {
		String stdOut = "";
		String stdErr = "";
		
		@Override
		public void handleOutput(char c) {		
		}

		@Override
		public void handleOutputLine(String line) {
			stdOut += line;
			
		}

		@Override
		public void handleErrorLine(String line) {
			stdErr += line;
			
		}
		
		public String getStdOut() {
			return stdOut;
		}
		
		public String getStdErr() {
			return stdErr;
		}
		
	}
}
