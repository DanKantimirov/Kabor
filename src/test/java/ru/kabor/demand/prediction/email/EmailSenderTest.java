package ru.kabor.demand.prediction.email;

import static org.junit.Assert.*;

import javax.mail.MessagingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailSenderTest {

	@Autowired
	EmailSender sender;
	
	@Test
	public void dummy(){
		assertTrue(true);
	}
	
	//@Test
	public void messageWithResult() {
		try {
			sender.sendMessageWithResult(11L);
		} catch (MessagingException|EmailSenderException e) {
			assertTrue("Some exception:" + e.toString(), false);
		}
		assertTrue(true);
	}
	
	//@Test
	public void messageWithError() {
		try {
			sender.sendMessageWithError(11L);
		} catch (MessagingException|EmailSenderException e) {
			assertTrue("Some exception:" + e.toString(), false);
		}
		assertTrue(true);
	}
	
	
	//@Test
	public void messageWithErrorAndBody() {
		try {
			sender.sendMessageWithError(11L, "Custom error");
		} catch (MessagingException|EmailSenderException e) {
			assertTrue("Some exception:" + e.toString(), false);
		}
		assertTrue(true);
	}

}
