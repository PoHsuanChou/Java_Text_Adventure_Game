package edu.uob;

import com.alexmerz.graphviz.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

// PLEASE READ:
// The tests in this file will fail by default for a template skeleton, your job is to pass them
// and maybe write some more, read up on how to write tests at
// https://junit.org/junit5/docs/current/user-guide/#writing-tests
final class BasicCommandTests {

  private GameServer server;

  // Make a new server for every @Test (i.e. this method runs before every @Test test case)
  @BeforeEach
  void setup() throws IOException, ParseException, ParserConfigurationException, SAXException {
      File entitiesFile = Paths.get("config/basic-entities.dot").toAbsolutePath().toFile();
      File actionsFile = Paths.get("config/basic-actions.xml").toAbsolutePath().toFile();
      server = new GameServer(entitiesFile, actionsFile);
  }

  // Test to spawn a new server and send a simple "look" command
  @Test
  void testLookingAroundStartLocation() {
    String response = server.handleCommand("player1: look").toLowerCase();
    assertTrue(response.contains("empty room"), "Did not see description of room in response to look");
    assertTrue(response.contains("magic potion"), "Did not see description of artifacts in response to look");
    assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
    assertTrue(response.contains("forest"), "Did not see the path to forest");
  }
  @Test
  void testGoTo(){
    String response1 = server.handleCommand("player1: goto ABC");
    assertTrue(response1.contains("no such room"), "should return no such room ");
    String response = server.handleCommand("player1: goto forest").toLowerCase();
    assertTrue(response.contains("you are in forest"),"Did not see description of action response to goto");
    server.handleCommand("player1: get the key");
    server.handleCommand("player1: open key");
    String response3 = server.handleCommand("player1: goto cellar");
    assertTrue(response3.contains("cellar"));
  }
  @Test
  void testLookingAroundForest(){
    server.handleCommand("player1: goto forest");
    String response1 = server.handleCommand("player1: look");
    assertTrue(response1.contains("dark forest"),"Did not see description of action response to goto");
    assertTrue(response1.contains("Brass key"),"Did not see description of artifacts in response to look");
    assertTrue(response1.contains("cabin"), "Did not see the path to cabin");
  }
  @Test
  void testGetArtefacts(){
    server.handleCommand("player1: look");
    server.handleCommand("player1: goto forest");
    String response = server.handleCommand("player1: get a pen").toLowerCase();
    assertTrue(response.contains("can not pick up"),"Can not get object not in this room");
    String response1 = server.handleCommand("player1: get a key");
    assertTrue(response1.contains("pick up a key"), "Did not see you pick up the key");
  }
  @Test
  void testInventory(){
    server.handleCommand("player1: look");
    server.handleCommand("player1: goto forest");
    server.handleCommand("player1: get a key");
    String response = server.handleCommand("player1: inventory");
    assertTrue(response.contains("key"),"Did not see object in the inventory");
    String response1 = server.handleCommand("player1: inv");
    assertTrue(response1.contains("key"),"Did not see object in the inventory");
    server.handleCommand("player1: drop key");
    String response2 = server.handleCommand("player1: inv");
    assertTrue(response.contains(""),"Should be empty");
  }
  @Test
  void testDrop(){
    server.handleCommand("player1: look");
    server.handleCommand("player1: goto forest");
    server.handleCommand("player1: get a key");
    String response = server.handleCommand("player1: drop key");
    assertTrue(response.contains("drop a key"),"Did not see you drop the key");
  }
  @Test
  void testAction(){
    server.handleCommand("player1: look");
    server.handleCommand("player1: goto forest");
    server.handleCommand("player1: get a key");
    String response = server.handleCommand("player1: ABCDEF");
    assertTrue(response.contains("something wrong with the trigger"),"this is not the trigger so should not pass");
    String response1 = server.handleCommand("player1: open with the ABC");
    assertTrue(response1.contains("wrong with the subject"),"the subject is wrong");
    assertTrue(response1.contains("wrong with the trigger"),"the subject is wrong");
    String response2 = server.handleCommand("player1: open with the key");
    assertTrue(response2.contains("unlock the trapdoor"),"did not unlock the cellar");
  }
  @Test
  void testAttack(){
    server.handleCommand("player1: look");
    server.handleCommand("player1: goto forest");
    server.handleCommand("player1: get a key");
    server.handleCommand("player1: open with the key");
    server.handleCommand("player1: goto cellar");
    String response = server.handleCommand("player1: attack the Elf");
    assertTrue(response.contains("lose some health"),"should lose health");
    String response1 = server.handleCommand("player1: hit the Elf");
    assertTrue(response1.contains("lose some health"),"should lose health");
    String response2 = server.handleCommand("player1: fight the Elf");
    assertTrue(response2.contains("lose some health"),"should lose health");
  }

  @Test
  void testPotion(){
    server.handleCommand("player1: get potion");
    server.handleCommand("player1: goto forest");
    server.handleCommand("player1: get a key");
    server.handleCommand("player1: open with the key");
    server.handleCommand("player1: goto cellar");
    String response = server.handleCommand("player1: drink the potion");
    assertTrue(response.contains("health improves"),"your health do not improves");
  }



  // Add more unit tests or integration tests here.

}
