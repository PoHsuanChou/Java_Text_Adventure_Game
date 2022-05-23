package edu.uob;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;

/** This class implements the STAG server. */
public final class GameServer {
    private static final char END_OF_TRANSMISSION = 4;
    private final TreeMap<String, HashSet<GameAction>> gameActionTree = new TreeMap<String, HashSet<GameAction>>();
    private final ArrayList<Locations> wholeMap = new ArrayList<>();
    private final ArrayList<Players> currentPlayerList = new ArrayList<>();
    public HashMap<String, ArrayList<String>> wholeRoute= new HashMap<>();
    public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException, SAXException {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }
    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer(File, File)}) otherwise we won't be able to mark
    * your submission correctly.
    *
    * <p>You MUST use the supplied {@code entitiesFile} and {@code actionsFile}
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    *
    */
    public GameServer(File entitiesFile, File actionsFile) throws IOException, ParseException, ParserConfigurationException, SAXException {
        Parser parser = new Parser();
        FileReader reader1 = new FileReader(entitiesFile);
        parser.parse(reader1);
        Graph wholeDocument = parser.getGraphs().get(0);
        ArrayList<Graph> sections = wholeDocument.getSubgraphs();
        ArrayList<Graph> locations = sections.get(0).getSubgraphs();
        storeData(wholeMap,locations); //store the whole location with all the objects, furniture, and subjects
        storeMap( sections);//store the route
        storeAction(actionsFile,gameActionTree);//store the whole actions
        // TODO implement your server logic here
    }
    public void storeData(ArrayList<Locations> wholeMap,ArrayList<Graph> locations){
        for (Graph location : locations) {
            Node locationDetails = location.getNodes(false).get(0);
            String locationName = locationDetails.getId().getId();
            String locationDesc = location.getNodes(false).get(0).getAttribute("description");
            Locations eachLocation = new Locations(locationName, locationDesc);
            for (int j = 0; j < location.getSubgraphs().size(); j++) {
                String Attribute = location.getSubgraphs().get(j).getId().getId();
                artefactsInMap(Attribute, location, eachLocation, j);
                furnitureInMap(Attribute, location, eachLocation, j);
                charactersInMap(Attribute, location, eachLocation, j);
            }
            wholeMap.add(eachLocation);
        }
    }
    public void artefactsInMap(String Attribute,Graph location,Locations eachLocation,int j){
        if(Attribute.contains("artefacts")){
            for(int k = 0; k < location.getSubgraphs().get(j).getNodes(false).size();k++){
                String artefactDesc = location.getSubgraphs().get(j).getNodes(false).get(k).getAttribute("description");
                String artefactObject = location.getSubgraphs().get(j).getNodes(false).get(k).getId().getId();
                eachLocation.addArtefacts(artefactObject,artefactDesc);
            }
        }
    }
    public void furnitureInMap(String Attribute,Graph location,Locations eachLocation,int j){
        if(Attribute.contains("furniture")){
            for(int k = 0; k < location.getSubgraphs().get(j).getNodes(false).size();k++){
                String furnitureDesc = location.getSubgraphs().get(j).getNodes(false).get(k).getAttribute("description");
                String furnitureObject = location.getSubgraphs().get(j).getNodes(false).get(k).getId().getId();
                eachLocation.addFurniture(furnitureObject,furnitureDesc);
            }
        }
    }
    public void charactersInMap(String Attribute,Graph location,Locations eachLocation,int j){
        if(Attribute.contains("characters")){
            for(int k = 0; k < location.getSubgraphs().get(j).getNodes(false).size();k++){
                String charactersDesc = location.getSubgraphs().get(j).getNodes(false).get(k).getAttribute("description");
                String charactersObject = location.getSubgraphs().get(j).getNodes(false).get(k).getId().getId();
                eachLocation.addCharacters(charactersObject,charactersDesc);
            }
        }
    }
    public void storeMap(ArrayList<Graph> sections){
        for(int i = 0; i < sections.get(1).getEdges().size(); i ++){
            String node = sections.get(1).getEdges().get(i).toString();
            String modifyNode = node.replace(";"," ");
            String[] eachNode = modifyNode.split(" ");
            if(wholeRoute.containsKey(eachNode[0])){
                wholeRoute.get(eachNode[0]).add(eachNode[2]);
            }
            else{
                ArrayList<String> create = new ArrayList<>();
                create.add(eachNode[2]);
                wholeRoute.put(eachNode[0],create);
            }
        }
    }
    public void storeAction(File actionsFile,TreeMap<String, HashSet<GameAction>> gameActionTree) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(actionsFile);
        Element root = document.getDocumentElement();
        NodeList actions = root.getChildNodes();
        for(int i = 1; i < actions.getLength(); i +=2 ){
            Element Actions = (Element)actions.item(i);
            Element triggers = (Element)Actions.getElementsByTagName("triggers").item(0);
            for(int k = 0; k < triggers.getElementsByTagName("keyword").getLength();k ++){
                GameAction gameAction = new GameAction();
                String eachActionName = storeEachAction(triggers,Actions,k,gameAction);
                if(gameActionTree.containsKey(eachActionName)){
                    gameActionTree.get(eachActionName).add(gameAction);
                }
                else{
                    HashSet<GameAction> gameActionHash = new HashSet<GameAction>();
                    gameActionHash.add(gameAction);
                    gameActionTree.put(eachActionName,gameActionHash);
                }
            }
        }
    }

    public String storeEachAction(Element triggers,Element Actions, int k,GameAction gameAction){
        String TriggerPhrase = triggers.getElementsByTagName("keyword").item(k).getTextContent();
        gameAction.setTrigger(TriggerPhrase);
        Element subjects = (Element)Actions.getElementsByTagName("subjects").item(0);
        for(int j = 0; j < subjects.getElementsByTagName("entity").getLength();j ++){
            String subjectPhrase = subjects.getElementsByTagName("entity").item(j).getTextContent();
            gameAction.addSubjectsArray(subjectPhrase);
        }
        Element consumed = (Element)Actions.getElementsByTagName("consumed").item(0);
        for(int j = 0; j < consumed.getElementsByTagName("entity").getLength();j ++){
            String consumedPhrase = consumed.getElementsByTagName("entity").item(j).getTextContent();
            gameAction.addConsumedArray(consumedPhrase);

        }
        Element produced = (Element)Actions.getElementsByTagName("produced").item(0);
        for(int j = 0; j < produced.getElementsByTagName("entity").getLength();j ++){
            String producedPhrase = produced.getElementsByTagName("entity").item(j).getTextContent();
            gameAction.addProducedArray(producedPhrase);
        }
        Element narration = (Element)Actions.getElementsByTagName("narration").item(0);
        String narrativePhrase = narration.getTextContent();
        gameAction.addNarrativeArray(narrativePhrase);
        return TriggerPhrase;
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.GameServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming game commands and carries out the corresponding actions.
    */
    public String handleCommand(String command) {
        ArrayList<String> basicCommandArr = new ArrayList<>();
        createBasicCommand(basicCommandArr);
        String[] theLine;
        theLine = command.toLowerCase().trim().split("\\s+");
        String player = theLine[0].replace(":","");
        String[] newLine = Arrays.copyOfRange(theLine, 1, theLine.length);
        Players currentPlayer  = setPlayer( player);
        if(newLine.length==0){
            System.out.println("this is empty");
            return "this is empty, please enter something";
        }
        String location = currentPlayer.getLocation();
        int room = -1;
        room = getRoom(location); // get the room number in the wholeMap array
        String triggerOrCommand = findTheBasicCommand(newLine,basicCommandArr);
        if(basicCommandArr.contains(triggerOrCommand)){
            return basicCommand(newLine,triggerOrCommand,room,currentPlayer);
        }
        String actionTrigger = findTheAction(newLine);
        if(actionTrigger == null){
            return "there is no right action in the command";
        }
        int actionSeq = checkOnlyAction(actionTrigger, newLine); // the action need to be unique
        if(actionSeq == -1){
            return "something wrong with the trigger or something wrong with the subject command";
        }
        GameAction action = checkAction(actionTrigger,actionSeq);
        boolean check = checkActionAndEntities(action,currentPlayer);
        if(!check){
            return "your command can not use in this room";
        }
        storeInStoreRoom(action,currentPlayer);
        moveTheObject(action,currentPlayer);
        String response = producedAction(action,triggerOrCommand,currentPlayer);
//         TODO implement your server logic here
        return response;


    }
    public String findTheAction(String[] newLine){
        String actionTrigger = null;
        for (String s : newLine) {
            if (gameActionTree.containsKey(s)) {
                actionTrigger = s;
            }
        }
        return actionTrigger;
    }
    public Players setPlayer(String player){
        Players currentPlayer = null;
        if(currentPlayerList.isEmpty()){
            Players newPlayer = new Players();
            newPlayer.setName(player);
            currentPlayerList.add(newPlayer);
            currentPlayer = newPlayer;
            currentPlayer.setLocation(wholeMap.get(0).getName());
            return currentPlayer;
        }
        for(int i = 0; i < currentPlayerList.size(); i ++){
            if(!currentPlayerList.get(i).getName().contains(player)){
                Players newPlayer = new Players();
                newPlayer.setName(player);
                currentPlayerList.add(newPlayer);
                currentPlayer = newPlayer;
                currentPlayer.setLocation(wholeMap.get(0).getName());
            }
            else{
                currentPlayer = currentPlayerList.get(i);
            }
            return currentPlayer;
        }
        return currentPlayer;
    }
    public void createBasicCommand(ArrayList<String> basicCommandArr){
        basicCommandArr.add("inventory");
        basicCommandArr.add("inv");
        basicCommandArr.add("get");
        basicCommandArr.add("drop");
        basicCommandArr.add("goto");
        basicCommandArr.add("look");
    }
    public String findTheBasicCommand(String[] newLine,ArrayList<String> basicCommandArr){
        String basicCommand = null;
        int count = 0;
        for (String s : newLine) {
            if (basicCommandArr.contains(s)) {
                basicCommand = s;
                count++;
            }
        }
        if(count>1||basicCommand == null){
            return "error";
        }
        else{
            return basicCommand;
        }
    }
    public GameAction checkAction(String triggerOrCommand,int actionSeq){
        Iterator<GameAction> Seq = gameActionTree.get(triggerOrCommand).iterator();
        GameAction action = null;
        if(actionSeq == 0) {
            action = Seq.next();
        }
        else{
            for(int i = 0;i <= actionSeq; i ++){
                action  = Seq.next();
            }
            assert action != null;
        }
        return action;
    }
    public void storeInStoreRoom(GameAction action,Players currentPlayer){
        int storeRoomNum  = wholeMap.size()-1;
        String location = currentPlayer.getLocation();
        int room = getRoom(location);
        for(int i = 0; i < action.getConsumedArray().size(); i ++){
            String target = action.getConsumedArray().get(i);
            for(int j = 0; j < wholeMap.get(room).getFurniture().size(); j++){
                if(wholeMap.get(room).getFurniture().get(j).name.equalsIgnoreCase(target)){
                    wholeMap.get(storeRoomNum).addFurniture(target,wholeMap.get(room).getFurniture().get(j).description);
                }
            }
            for(int j = 0; j < wholeMap.get(room).getCharacters().size(); j ++){
                if(wholeMap.get(room).getCharacters().get(j).name.equalsIgnoreCase(target)){
                    wholeMap.get(storeRoomNum).addCharacters(target,wholeMap.get(room).getCharacters().get(j).description);
                }
            }
            for(int j = 0; j < wholeMap.get(room).getArtefacts().size(); j ++){
                if(wholeMap.get(room).getArtefacts().get(j).name.equalsIgnoreCase(target)){
                    wholeMap.get(storeRoomNum).addArtefacts(target,wholeMap.get(room).getArtefacts().get(j).description);
                }
            }
        }
        String invString = String.valueOf(currentPlayer.getInventory().keySet());
        String inv = invString.replace("[", "").replace("]","").replace(" ","");
        String[] inventory = inv.split(",");
        for (String s : inventory) {
            if (action.getConsumedArray().contains(s)) {
                currentPlayer.getInventory().remove(s);
            }
        }
    }
    public void moveTheObject(GameAction action,Players currentPlayer){
        String location = currentPlayer.getLocation();
        int room = getRoom(location);
            int target = -1;
                for(int j = 0; j <wholeMap.get(room).getArtefacts().size(); j ++ ){
                    if(action.getConsumedArray().contains(wholeMap.get(room).getArtefacts().get(j).getName())){
                        target = j;
                        wholeMap.get(room).getArtefacts().remove(target);
                        j--;
                    }
                }
                for(int k = 0; k < wholeMap.get(room).getFurniture().size(); k ++){
                    if(action.getConsumedArray().contains(wholeMap.get(room).getFurniture().get(k).getName())){
                        target = k;
                        wholeMap.get(room).getFurniture().remove(target);
                        k--;
                    }
                }
                for(int l = 0; l < currentPlayer.getInventory().size();l++){
                    if(action.getConsumedArray().contains(currentPlayer.getInventory().get(l))){
                        currentPlayer.getInventory().remove(l);
                        l--;
                    }
                }
    }
    public boolean checkActionAndEntities(GameAction action,Players currentPlayer){
        String location = currentPlayer.getLocation();
        int room = getRoom(location);
            ArrayList<String> tem = new ArrayList<>();
            for(int i = 0; i < wholeMap.get(room).getFurniture().size();i++){
                tem.add(wholeMap.get(room).getFurniture().get(i).name);
            }
            for(int k = 0; k < wholeMap.get(room).getCharacters().size(); k++){
                tem.add(wholeMap.get(room).getCharacters().get(k).name);
            }
            tem.add("health");
            String invString = String.valueOf(currentPlayer.getInventory().keySet());
            String inv = invString.replace("[", "").replace("]","").replace(" ","");
            String[] inventory = inv.split(",");
            Collections.addAll(tem, inventory);
            for(int k = 0; k < action.getConsumedArray().size(); k ++){
                if(!tem.contains(action.getConsumedArray().get(k))){
                    return false;
                }
            }
        return true;
    }
    public String producedAction(GameAction action,String trigger,Players currentPlayer){
        String location = currentPlayer.getLocation();
        int room = getRoom(location);
            produceLocation(room,action,location);
            produceHealth(action,trigger,currentPlayer);
            loseHealth(action,trigger,currentPlayer);
             produceObject(room,action);
        return action.getNarrativeArray().get(0);
    }
    public void loseHealth(GameAction action,String trigger,Players currentPlayer){
        if(action.getConsumedArray().contains("health")){
            if(trigger.equalsIgnoreCase("fight")||trigger.equalsIgnoreCase("hit")||trigger.equalsIgnoreCase("attack")){
                currentPlayer.setHealthLevel(-1);
            }
        }
    }
    public void produceHealth(GameAction action,String trigger,Players currentPlayer){
        if(action.getConsumedArray().contains("potion")){
            if(trigger.equalsIgnoreCase("drink")){
                currentPlayer.setHealthLevel(1);
                action.getConsumedArray().remove("potion");
            }
        }
    }
    public void produceLocation(int room,GameAction action,String location){
        for(int j = 0; j < action.getProducedArray().size(); j ++){
            String addLocation = action.getProducedArray().get(j);
            if(wholeRoute.containsKey(addLocation)){
                wholeRoute.get(location).add(addLocation);
                action.getProducedArray().remove(addLocation);
                j--;
            }
        }
    }
    public void produceObject(int room,GameAction action){
        int storeRoomNum  = wholeMap.size()-1;
        for(int i = 0; i < action.getProducedArray().size(); i ++){
            String target = action.getProducedArray().get(i);
            for(int j = 0; j < wholeMap.get(storeRoomNum).getArtefacts().size(); j ++){
                if(wholeMap.get(storeRoomNum).getArtefacts().get(j).getName().equalsIgnoreCase(target)){
                    wholeMap.get(room).addArtefacts(target,wholeMap.get(storeRoomNum).getArtefacts().get(j).description);
                }
            }
            for(int j = 0; j < wholeMap.get(storeRoomNum).getCharacters().size();j++){
                if(wholeMap.get(storeRoomNum).getCharacters().get(j).getName().equalsIgnoreCase(target)){
                    wholeMap.get(room).addCharacters(target,wholeMap.get(storeRoomNum).getCharacters().get(j).description);
                }
            }
            for(int j = 0; j < wholeMap.get(storeRoomNum).getFurniture().size();j++){
                if(wholeMap.get(storeRoomNum).getFurniture().get(j).getName().equalsIgnoreCase(target)){
                    wholeMap.get(room).addFurniture(target,wholeMap.get(storeRoomNum).getFurniture().get(j).description);
                }
            }
        }
    }
    public int checkOnlyAction(String trigger,String[] newLine) {
        int actionCount = 0;
        int theActionNum = -1;
        int moreMatch = 0;
        int seq = -1;
        for (GameAction action : gameActionTree.get(trigger)) {
            theActionNum++;
            int matchWord = 0;
            List<String> eachArr = action.getSubjectsArray();
            for(int i = 0; i < newLine.length; i ++){
                if(eachArr.contains(newLine[i])){
                    matchWord++;
                }
            }
            if (matchWord >= 1) {
                actionCount++;
                if (actionCount > 1 && moreMatch == matchWord) {
                    return -1;
                }
                if (moreMatch < matchWord) {
                    moreMatch = matchWord;
                    seq = theActionNum;
                }
            }
        }
        return seq;
    }
    public String basicCommand(String[] Command,String triggerOrCommand,int room,Players currentPlayer) {
                if(triggerOrCommand.compareToIgnoreCase("inventory") == 0 || triggerOrCommand.compareToIgnoreCase("inv") == 0){
                    HashMap<String,String> list = inventoryCommand(currentPlayer);
                    return String.valueOf(list.keySet());
                }
                if(triggerOrCommand.compareToIgnoreCase("get") == 0){
                    if(Command.length == 1){
                        return "what do you want to pick up";
                    }
                    String pickUpObject= getObject(room,Command);
                    return getCommand(pickUpObject,currentPlayer);
                }
                if(triggerOrCommand.compareToIgnoreCase("drop") == 0){
                    if(Command.length == 1){
                        return "what do you want to drop";
                    }
                    String dropObject = dropObject(Command,currentPlayer);
                    return dropCommand(dropObject,currentPlayer);
                }
                if(triggerOrCommand.compareToIgnoreCase("goto") == 0){
                    if(Command.length == 1){
                        return "what do you want to go";
                    }
                    String goTo = Command[1];
                    return gotoCommand(goTo,currentPlayer);
                }
                if(triggerOrCommand.compareToIgnoreCase("look") == 0){
                    if(Command.length == 1){
                        return lookCommand(currentPlayer);
                    }
                    else{
                        return "Can only use look";
                    }
                }
        return "something wrong with basic command";
    }
    public String dropObject(String[]Command,Players currentPlayer){
        String dropObject = null;
        String invString = String.valueOf(currentPlayer.getInventory().keySet());
        String inv = invString.replace("[", "").replace("]","").replace(" ","");
        String[] inventory = inv.split(",");
        for(int i = 0; i < inventory.length; i ++){
            for(int j = 0; j < Command.length; j ++){
                if(Command[j].equalsIgnoreCase(inventory[i])){
                    dropObject = inventory[i];
                    break;
                }
            }
        }
        if(dropObject == null){
            System.out.println("error");
            return "error";
        }
        return dropObject;
    }
    public String getObject(int room, String[]Command){
        String pickUpObject = null;
        for(int i = 0; i < wholeMap.get(room).getArtefacts().size(); i ++){
            for(int j = 0; j < Command.length; j ++){
                if( wholeMap.get(room).getArtefacts().get(i).getName().equalsIgnoreCase(Command[j])){
                    pickUpObject = Command[j];
                }
            }

        }
        if(pickUpObject==null){
            return "error";
        }
        return pickUpObject;
    }
    public String gotoCommand(String goTo,Players currentPlayer){
        int checkRoom = -1;
        checkRoom = getRoom(goTo);
        if(checkRoom == -1){
            return "there is no such room";
        }
        currentPlayer.setLocation(goTo);
        return "now you are in " + goTo;
    }
    public String lookCommand(Players currentPlayer) {
        String location = currentPlayer.getLocation(); // current location
        int room = -1;
        room = getRoom(location);
        String chooseOfPath = null;
        if (wholeRoute.containsKey(location)) {
            ArrayList<String> path = new ArrayList<>(wholeRoute.get(location));
            String[] pathList = ArrayListToStringArr(path);
            chooseOfPath = Arrays.toString(pathList).replace("["," ").replace("]"," ").replace(",","\n");
        } else {
            System.out.println("there is no such location");
        }
        String allEntities = allEntitiesStr(room);
        String response = "you are now in " + location + " and this is " + wholeMap.get(room).description+". you can see: " + "\n" + allEntities +
                "\n" + "you can access from here: " +"\n" +  chooseOfPath;
        return response;
    }
    public String allEntitiesStr(int room){
        ArrayList<String> all = new ArrayList<>();
        if(wholeMap.get(room).getArtefacts().size()>0){
            for(int i = 0; i < wholeMap.get(room).getArtefacts().size(); i ++){
                all.add("there is a " + wholeMap.get(room).getArtefacts().get(i).description);
            }
        }
        if(wholeMap.get(room).getFurniture().size()>0){
            for(int j = 0; j < wholeMap.get(room).getFurniture().size(); j ++){
                all.add("there is a " + wholeMap.get(room).getFurniture().get(j).description);
            }
        }
        if(wholeMap.get(room).getCharacters().size()>0){
            for(int k = 0; k < wholeMap.get(room).getCharacters().size(); k ++){
                all.add("there is a " +  wholeMap.get(room).getCharacters().get(k).description);
            }
        }
        String[] allArr = ArrayListToStringArr(all);
        String str = Arrays.toString(allArr);
        String newStr = str.replace("["," ").replace("]"," ").replace(",","\n");
        return newStr;

    }
    public int getRoom(String location){
        for(int i = 0; i < wholeMap.size(); i ++){
            if(wholeMap.get(i).getName().compareToIgnoreCase(location)==0){
                return i;
            }
        }
        return -1;
    }
    public String dropCommand(String dropObject,Players currentPlayer){
        String location = currentPlayer.getLocation();
        int room = -1;
        room = getRoom(location);
        if(!currentPlayer.getInventory().containsKey(dropObject)){
            return "there is no such item in your inventory";
        }
        wholeMap.get(room).addArtefacts(dropObject,currentPlayer.getInventory().get(dropObject));
        currentPlayer.getInventory().remove(dropObject);
        return "you just drop a "+ dropObject;
    }
    public String getCommand(String pickUpObject,Players currentPlayer){
        String location = currentPlayer.getLocation();
        int room = -1;
        room = getRoom(location);
        for(int i = 0; i < wholeMap.get(room).getArtefacts().size(); i ++){
            if(wholeMap.get(room).getArtefacts().get(i).getName().compareToIgnoreCase(pickUpObject)==0){
                currentPlayer.addInventory(pickUpObject,wholeMap.get(room).getArtefacts().get(i).description);
                wholeMap.get(room).getArtefacts().remove(i);
                return "you pick up a " + pickUpObject;
            }
        }
        return "you can not pick up this in this room";
    }
    public HashMap<String,String> inventoryCommand(Players currentPlayer){
        return currentPlayer.getInventory();
    }
    public String[] ArrayListToStringArr(ArrayList<String> List){
        String[] str = new String[List.size()];
        for (int i = 0; i < List.size(); i++) {
            str[i] = List.get(i);
        }
        return str;
    }

    //  === Methods below are there to facilitate server related operations. ===

    /**
    * Starts a *blocking* socket server listening for new connections. This method blocks until the
    * current thread is interrupted.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * you want to.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Handles an incoming connection from the socket server.
    *
    * <p>This method isn't used for marking. You shouldn't have to modify this method, but you can if
    * * you want to.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();

            }
        }
    }
}
