package com.xebialabs.overthere.nio.file;

import java.util.List;

import org.testng.annotations.DataProvider;

public class BaseTest {
	
	@DataProvider(name = "protocol")
	public Object[][] protocolDataProvider() {
		List<OverthereProtocol> testList = OverthereProtocol.testableValuesList();
		
		int vals = testList.size();
		
		int i = 0;
		Object[][] output = new Object[vals][1];
		for (OverthereProtocol protocol : testList ) {
			output[i][0] = protocol;
			i++;
		}
		
		return output;
	}

}
