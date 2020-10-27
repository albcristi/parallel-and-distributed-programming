package model;

import com.sun.tools.javac.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyScanner {
    private static final String PATH_TO_TOKENS = "token.in";
    private List<String> tokens;
    private String sourceProgram;
    private SymbolTable symbolTable;
    private List<Pair<String, Pair<Integer,Integer>>> pif; // smth like Token/CT/ID position in the st or (0,-1) for Tokens
    private List<String> errorList = new ArrayList<>();
    public MyScanner(String pathToSourceProgram) {
        this.tokens = new ArrayList<>();
        this.sourceProgram = pathToSourceProgram;
        this.symbolTable = new SymbolTable(29);
        this.pif = new ArrayList<>();
        loadTokens();
    }

    private void loadTokens() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(MyScanner.PATH_TO_TOKENS));
            String line = "";
            while (true){
                line = reader.readLine();
                if(line == null || line.equals(""))
                    break;
                tokens.add(line.toString());
            }
            reader.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Boolean isConstant(String token) {
        return token.matches("\\-?[1-9]+[0-9]*|0")
                || token.matches("\"[a-zA-Z0-9 _]+\"")
                || token.equals("true")
                || token.equals("false");
    }

    private Boolean isIdentifier(String token){
        return token.matches("(^[a-zA-Z][a-zA-Z0-9]*)");
    }


    public void scan(){
        Integer currentLineNumber = 1;

        try{
            BufferedReader reader = new BufferedReader(new FileReader(this.sourceProgram));
            String currentLine = "";
            while (true){
                currentLine = reader.readLine();
                if(currentLine == null || currentLine == "")
                    break;
                // removing extra spaces
                currentLine = currentLine.trim().replaceAll(" {2,}", " ");
                if(!currentLine.equals("")){
                    // now we have to actually start classifying our tokens
                    List<String> detectedTokens = Arrays.asList(currentLine.split(" "));
                    // we need to parse the list of reserved words,separators, etc
                    detectedTokens = tokenize(detectedTokens);
                    //we need to make sure our numerical constants are well formed
                    detectedTokens = tokenizeFinalVersion(detectedTokens);
                    //System.out.println(detectedTokens);
                    //after this part we do the classification of each token
                    // and add to PIF and ST the necessary data
                    for(String token: detectedTokens) {
                        // in the end we should not forget that we may have a lexical error
                        // so we should do checks for those and add the error to the errorList
                        if(this.tokens.contains(token)){
                           // we detected a special token
                            // we add it to the PIF
                            pif.add(new Pair<>(token, new Pair<>(0,-1)));
                        }
                        else {
                            // we need to see if we deal with an identifier or a constant
                            if(isIdentifier(token) || isConstant(token)){
                                Pair<Integer, Integer> position = symbolTable.search(token);
                                String whatIS = isIdentifier(token) ? "ID" : "CONST";
                                if(position.fst == -1)
                                    position = symbolTable.add(token);
                                pif.add(new Pair<>(whatIS,position));
                            }
                            else{
                                // LEXICAL ERROR
                                errorList.add("Lexical Error: token "+token+" can not be identified! Line Number:"+currentLineNumber);
                            }
                        }
                    }
                }
                currentLineNumber += 1; // time to move to the next line
            }
            reader.close();
            writeST();
            writePIF();
            if(errorList.size()==0)
                System.out.println("Lexically correct");
            else {
                System.out.println("Lexically incorrect");
                for(String str: errorList)
                    System.out.println(str);
            }
            writeLexicalErrors();
        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> tokenize(List<String> lineTokens){
        for(String separator: this.tokens){
                List<String> updatedTokens = new ArrayList<>();
                for(String str: lineTokens) {
                    if (str.contains(separator)) {
                        // do special tokenize operation
                        List<String> split = mySpecialSplit(str, separator);
                        updatedTokens.addAll(split);
                    } else {
                        updatedTokens.add(str);
                    }
                }
                lineTokens = updatedTokens;
            }
        lineTokens = lineTokens.stream().filter(e->!e.equals("")).collect(Collectors.toList());
        return lineTokens;
    }

    private List<String> mySpecialSplit(String string, String tok) {
        List<String> result = new ArrayList<>();
        if (string.equals(tok)) {
            result.add(string);
            return result;
        }
        Integer index = string.indexOf(tok);
        while (index != -1) {
            if (index > 0)
                if(!string.substring(0,index).equals(" "))
                    result.add(string.substring(0, index));
            result.add(tok);
            string = string.substring(tok.length() + index);
            index = string.indexOf(tok);
        }
        if(string.length()>0){
            result.add(string);
        }
        return result;
    }

    private List<String> tokenizeFinalVersion(List<String> tokens){
        tokens = tokenizeSpecialCasesForEqualSign(tokens);
        tokens = specialInputOutputCommands(tokens);
        tokens = specialStringConstants(tokens);
        tokens = specialIntegerConstants(tokens);
        return tokens;
    }

    private List<String> tokenizeSpecialCasesForEqualSign(List<String> tokens){
        if(!tokens.toString().contains("="))
            return tokens;
        /*
        In case of = we have the following special cases:
                    ==
                    <=
                    >=
         */
        List<String> betterVersion = new ArrayList<>();
        for(int i=0; i<tokens.size()-1; i++)
            if(tokens.get(i+1).equals("=") &&
                "=><".contains(tokens.get(i))){
                betterVersion.add(tokens.get(i)+tokens.get(i+1));
                i++;
            }
            else{
                betterVersion.add(tokens.get(i));
                if(i+1==tokens.size()-1)
                    betterVersion.add(tokens.get(i+1));
            }
        return betterVersion;
    }

    private List<String> specialInputOutputCommands(List<String> tokens){
        /*
        In the case of the I/O operations we might have
                give, Inti
                say, Inti
        Those should be translated to sayInti, giveInti
         */
        if(!(tokens.contains("say") || tokens.contains("give"))&& !tokens.contains("Inti"))
            return tokens;
        List<String> tokenized = new ArrayList<>();
        for(int i=0; i<tokens.size()-1; i++)
            if((tokens.get(i).equals("say") || tokens.get(i).equals("give")) &&
                tokens.get(i+1).equals("Inti")){
                tokenized.add(tokens.get(i)+tokens.get(i+1));
                i++;
            }
            else{
                tokenized.add(tokens.get(i));
                if(i+1==tokens.size()-1)
                    tokenized.add(tokens.get(i+1));
            }
        return tokenized;
    }

    private List<String> specialStringConstants(List<String> tokens){
        if(!tokens.toString().contains("\""))
                return tokens;
        List<String> betterTokenized = new ArrayList<>();
        for(int i=0; i<tokens.size(); i++){
            if(tokens.get(i).contains("\"")
                    && tokens.get(i).length()>1
                    && tokens.get(i).substring(1).contains("\"")){
                betterTokenized.add(tokens.get(i));
            }
            else{
                if(tokens.get(i).contains("\"")){
                    // some compound string constant
                    String stringConstant = tokens.get(i);
                    for(int j=i+1; j<tokens.size(); j++) {
                        stringConstant += " "+tokens.get(j);
                        if (tokens.get(j).contains("\"")) {
                            i = j;
                            break;
                        }
                    }
                    betterTokenized.add(stringConstant);
                }
                else{
                    betterTokenized.add(tokens.get(i));
                }
            }
        }
        return betterTokenized;
    }

    private List<String> specialIntegerConstants(List<String> tokens){
        /*
        We can have number of form
                +no
                -no
       But: +/- also can represent arithmetic operations:
                no1+no2
                no1-no2
       In this way, we reunite -/+ with a no in case of:
                            - -/+ is the first token of the list followed by a number
                            - the token before -/+ is not a number/identifier and the token after is a number
         */
        if(tokens.size() <= 2)
            return tokens;
        List<String> tokenized = new ArrayList<>();

        for(int i=1; i<tokens.size()-1; i++){
           if((tokens.get(i).equals("+")||tokens.get(i).equals("-"))
                && isNumber(tokens.get(i+1))
                && !isNumber(tokens.get(i-1))
                && !isIdentifier(tokens.get(i-1))) {
               tokenized.add(tokens.get(i) + tokens.get(i + 1));
               i++;
           }
           else{
               if(i-1==0)
                   tokenized.add(tokens.get(0));
               tokenized.add(tokens.get(i));
               if(i+1==tokens.size()-1)
                   tokenized.add(tokens.get(i+1));
           }
        }
        return tokenized;
    }

    private Boolean isNumber(String str){
        try{
            Integer i = Integer.parseInt(str);
            return  true;
        }
        catch (Exception e){
            return false;
        }
    }

    private void writeST(){
        try{
            /*
              st.out file format
              ST SIZE
              1, id1-0, id2-2
              2,
              3,
              ...
              size-1, ct-0
              size, id4-0,ctn-1
             */
            FileWriter fileWriter = new FileWriter("./lexical-analysis-out/st.out");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Integer size = symbolTable.getSize();
            Node[] elems = symbolTable.getElements();

            printWriter.println(size);
            for(int i=0; i<size; i++){
                String line = i+",";
                Node currentNode = elems[i];
                while (currentNode!=null){
                    line += currentNode.identifier+"-"+currentNode.index+",";
                    currentNode = currentNode.nextNode;
                }
                printWriter.println(line);
            }

            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writePIF(){
        try{
            /*
             pif.out file format
             pif size
             1, ;,0,-1
             2, ct,1,1
             ...
             size-1, id,2,2
             size, ;,0,-1
             */
            FileWriter fileWriter = new FileWriter("./lexical-analysis-out/pif.out");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(pif.size());
            for(int i=0; i<this.pif.size(); i++){
                String line = i+",";
                line +=  pif.get(i).fst+","+pif.get(i).snd.fst+","+pif.get(i).snd.snd;
                printWriter.println(line);
            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeLexicalErrors(){
        try {
            FileWriter fileWriter = new FileWriter("./lexical-analysis-out/lexical-errors.out");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(errorList.size());
            String msg = errorList.size()>0 ? "lexically incorrect" : "lexically correct";
            printWriter.println(msg);
            for(String error: errorList)
                printWriter.println(error);
            printWriter.close();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
