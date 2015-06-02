package com.eas.script;

import com.eas.concurrent.DeamonThreadFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.IdentNode;
import jdk.nashorn.internal.ir.LexicalContext;
import jdk.nashorn.internal.ir.Node;
import jdk.nashorn.internal.ir.VarNode;
import jdk.nashorn.internal.ir.visitor.NodeOperatorVisitor;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;
import jdk.nashorn.internal.parser.Lexer;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.parser.Token;
import jdk.nashorn.internal.parser.TokenStream;
import jdk.nashorn.internal.parser.TokenType;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.JSType;
import jdk.nashorn.internal.runtime.ScriptEnvironment;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.ScriptRuntime;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.Undefined;
import jdk.nashorn.internal.runtime.options.Options;

/**
 *
 * @author vv, mg
 */
public class Scripts {

    public static final String THIS_KEYWORD = "this";//NOI18N

    private static final ThreadLocal<Space> spaceRef = new ThreadLocal<>();

    public static Space getSpace() {
        return spaceRef.get();
    }

    public static void setSpace(Space aSpace) {
        if (aSpace != null) {
            spaceRef.set(aSpace);
        } else {
            spaceRef.remove();
        }
    }

    public static class Space {

        protected ScriptEngine engine;
        protected Object global;
        protected Map<String, JSObject> publishers = new HashMap<>();

        public Space(ScriptEngine aEngine) {
            super();
            engine = aEngine;
        }

        protected JSObject loadFunc;
        protected JSObject toPrimitiveFunc;
        protected JSObject lookupInGlobalFunc;
        protected JSObject putInGlobalFunc;
        protected JSObject toDateFunc;
        protected JSObject parseJsonFunc;
        protected JSObject parseJsonWithDatesFunc;
        protected JSObject writeJsonFunc;
        protected JSObject extendFunc;
        protected JSObject scalarDefFunc;
        protected JSObject collectionDefFunc;
        protected JSObject isArrayFunc;
        protected JSObject makeObjFunc;
        protected JSObject makeArrayFunc;
        protected JSObject listenFunc;
        protected JSObject listenElementsFunc;

        public void setGlobal(Object aValue) {
            if (global == null) {
                global = aValue;
            } else {
                throw new IllegalStateException("Scripts space should be initialized only once.");
            }
        }

        public void putPublisher(String aClassName, JSObject aPublisher) {
            publishers.put(aClassName, aPublisher);
        }

        public JSObject getPublisher(String aClassName) {
            return publishers.get(aClassName);
        }

        public JSObject getLoadFunc() {
            assert loadFunc != null : SCRIPT_NOT_INITIALIZED;
            return loadFunc;
        }

        public void setLoadFunc(JSObject aValue) {
            assert loadFunc == null;
            loadFunc = aValue;
        }

        public JSObject getToPrimitiveFunc() {
            assert toPrimitiveFunc != null : SCRIPT_NOT_INITIALIZED;
            return toPrimitiveFunc;
        }

        public void setToPrimitiveFunc(JSObject aValue) {
            assert toPrimitiveFunc == null;
            toPrimitiveFunc = aValue;
        }

        public void setLookupInGlobalFunc(JSObject aValue) {
            assert lookupInGlobalFunc == null;
            lookupInGlobalFunc = aValue;
        }

        public void setPutInGlobalFunc(JSObject aValue) {
            assert putInGlobalFunc == null;
            putInGlobalFunc = aValue;
        }

        public JSObject getToDateFunc() {
            assert toDateFunc != null;
            return toDateFunc;
        }

        public void setToDateFunc(JSObject aValue) {
            assert toDateFunc == null;
            toDateFunc = aValue;
        }

        public void setParseJsonFunc(JSObject aValue) {
            assert parseJsonFunc == null;
            parseJsonFunc = aValue;
        }

        public void setParseJsonWithDatesFunc(JSObject aValue) {
            assert parseJsonWithDatesFunc == null;
            parseJsonWithDatesFunc = aValue;
        }

        public void setWriteJsonFunc(JSObject aValue) {
            assert writeJsonFunc == null;
            writeJsonFunc = aValue;
        }

        public void setExtendFunc(JSObject aValue) {
            assert extendFunc == null;
            extendFunc = aValue;
        }

        public void setScalarDefFunc(JSObject aValue) {
            assert scalarDefFunc == null;
            scalarDefFunc = aValue;
        }

        public void setCollectionDefFunc(JSObject aValue) {
            assert collectionDefFunc == null;
            collectionDefFunc = aValue;
        }

        public void setIsArrayFunc(JSObject aValue) {
            assert isArrayFunc == null;
            isArrayFunc = aValue;
        }

        public void setMakeObjFunc(JSObject aValue) {
            assert makeObjFunc == null;
            makeObjFunc = aValue;
        }

        public void setMakeArrayFunc(JSObject aValue) {
            assert makeArrayFunc == null;
            makeArrayFunc = aValue;
        }

        public void setListenFunc(JSObject aValue) {
            assert listenFunc == null;
            listenFunc = aValue;
        }

        public void setListenElementsFunc(JSObject aValue) {
            assert listenElementsFunc == null;
            listenElementsFunc = aValue;
        }

        public Object toJava(Object aValue) {
            if (aValue instanceof ScriptObject) {
                aValue = ScriptUtils.wrap((ScriptObject) aValue);
            }
            if (aValue instanceof JSObject) {
                assert toPrimitiveFunc != null : SCRIPT_NOT_INITIALIZED;
                aValue = toPrimitiveFunc.call(null, new Object[]{aValue});
            } else if (aValue == ScriptRuntime.UNDEFINED) {
                return null;
            }
            return aValue;
        }

        public Object toJs(Object aValue) {
            if (aValue instanceof Date) {// force js boxing of date, because of absence js literal of date value
                assert toDateFunc != null : SCRIPT_NOT_INITIALIZED;
                return toDateFunc.call(null, aValue);
            } else if (aValue instanceof HasPublished) {
                return ((HasPublished) aValue).getPublished();
            } else {
                return aValue;
            }
        }

        public Object[] toJs(Object[] aArray) {
            Object[] publishedArgs = new Object[aArray.length];
            for (int i = 0; i < aArray.length; i++) {
                publishedArgs[i] = toJs(aArray[i]);
            }
            return publishedArgs;
        }

        public Object parseJson(String json) {
            assert parseJsonFunc != null : SCRIPT_NOT_INITIALIZED;
            return parseJsonFunc.call(null, new Object[]{json});
        }

        public Object parseJsonWithDates(String json) {
            assert parseJsonWithDatesFunc != null : SCRIPT_NOT_INITIALIZED;
            return parseJsonWithDatesFunc.call(null, new Object[]{json});
        }

        public String toJson(Object aObj) {
            assert writeJsonFunc != null : SCRIPT_NOT_INITIALIZED;
            if (aObj instanceof Undefined) {//nashorn JSON parser could not work with undefined.
                aObj = null;
            }
            if (aObj instanceof JSObject || aObj instanceof CharSequence
                    || aObj instanceof Number || aObj instanceof Boolean || aObj instanceof ScriptObject || aObj == null) {
                return JSType.toString(writeJsonFunc.call(null, new Object[]{aObj}));
            } else {
                throw new IllegalArgumentException("Java object couldn't be converted to JSON!");
            }
        }

        public void extend(JSObject aChild, JSObject aParent) {
            assert extendFunc != null : SCRIPT_NOT_INITIALIZED;
            extendFunc.call(null, new Object[]{aChild, aParent});
        }

        public JSObject scalarPropertyDefinition(JSObject targetEntity, String targetFieldName, String sourceFieldName) {
            assert scalarDefFunc != null : SCRIPT_NOT_INITIALIZED;
            return (JSObject) scalarDefFunc.newObject(new Object[]{targetEntity, targetFieldName, sourceFieldName});
        }

        public JSObject collectionPropertyDefinition(JSObject sourceEntity, String targetFieldName, String sourceFieldName) {
            assert collectionDefFunc != null : SCRIPT_NOT_INITIALIZED;
            return (JSObject) collectionDefFunc.newObject(new Object[]{sourceEntity, targetFieldName, sourceFieldName});
        }

        public boolean isArrayDeep(JSObject aInstance) {
            assert isArrayFunc != null : SCRIPT_NOT_INITIALIZED;
            Object oResult = isArrayFunc.call(null, new Object[]{aInstance});
            return Boolean.TRUE.equals(oResult);
        }

        public JSObject makeObj() {
            assert makeObjFunc != null : SCRIPT_NOT_INITIALIZED;
            Object oResult = makeObjFunc.call(null, new Object[]{});
            return (JSObject) oResult;
        }

        public JSObject makeArray() {
            assert makeArrayFunc != null : SCRIPT_NOT_INITIALIZED;
            Object oResult = makeArrayFunc.call(null, new Object[]{});
            return (JSObject) oResult;
        }

        public JSObject listen(JSObject aTarget, String aPath, JSObject aCallback) {
            assert listenFunc != null : SCRIPT_NOT_INITIALIZED;
            Object oResult = listenFunc.call(null, new Object[]{aTarget, aPath, aCallback});
            return (JSObject) oResult;
        }

        public JSObject listenElements(JSObject aTarget, JSObject aCallback) {
            assert listenElementsFunc != null : SCRIPT_NOT_INITIALIZED;
            Object oResult = listenElementsFunc.call(null, new Object[]{aTarget, aCallback});
            return (JSObject) oResult;
        }

        public JSObject createModule(String aModuleName) {
            assert lookupInGlobalFunc != null : SCRIPT_NOT_INITIALIZED;
            Object oConstructor = lookupInGlobalFunc.call(null, new Object[]{aModuleName});
            if (oConstructor instanceof JSObject && ((JSObject) oConstructor).isFunction()) {
                JSObject jsConstructor = (JSObject) oConstructor;
                return (JSObject) jsConstructor.newObject(new Object[]{});
            } else {
                return null;
            }
        }

        public JSObject lookupInGlobal(String aName) {
            assert lookupInGlobalFunc != null : SCRIPT_NOT_INITIALIZED;
            Object res = lookupInGlobalFunc.call(null, new Object[]{aName});
            return res instanceof JSObject ? (JSObject) res : null;
        }

        public void putInGlobal(String aName, JSObject aValue) {
            assert putInGlobalFunc != null : SCRIPT_NOT_INITIALIZED;
            putInGlobalFunc.call(null, new Object[]{aName, aValue});
        }

        protected Map<URL, Executed> executed = new HashMap<>();

        public Object exec(URL aSourcePlace) throws ScriptException, URISyntaxException {
            assert loadFunc != null : SCRIPT_NOT_INITIALIZED;
            Executed eEntry = executed.get(aSourcePlace);
            long lastModified = Paths.get(aSourcePlace.toURI()).toFile().lastModified();
            if (eEntry == null || lastModified > eEntry.getTimeStamp()) {
                Object scriptRes = loadFunc.call(null, aSourcePlace.toString());
                executed.put(aSourcePlace, new Executed(lastModified, scriptRes));
                return scriptRes;
            } else {
                return eEntry.getScriptResult();
            }
        }

        public Object exec(String aSource) throws ScriptException, URISyntaxException {
            assert loadFunc != null : SCRIPT_NOT_INITIALIZED;
            return engine.eval(aSource);
        }

        protected Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        protected AtomicInteger queueVersion = new AtomicInteger();
        protected AtomicReference worker = new AtomicReference(null);

        public void process(Runnable aTask) {
            final Scripts.Space space = this;
            offerTask(() -> {
                Scripts.Space oldSpace = getSpace();
                setSpace(space);
                try {
                    Runnable processedTask = aTask;
                    int version;
                    int newVersion;
                    Thread thisThread = Thread.currentThread();
                    do {
                        version = queueVersion.get();
                        // Zombie counter ...
                        newVersion = version + 1;
                        if (newVersion == Integer.MAX_VALUE) {
                            newVersion = 0;
                        }
                        if (processedTask != null) {//Single attempt to offer aTask.
                            queue.offer(processedTask);
                            processedTask = null;
                        }
                        if (worker.compareAndSet(null, thisThread)) {// Worker electing.
                            // already single threaded environment
                            if (global == null) {
                                Bindings bindings = engine.createBindings();
                                bindings.put("space", space);
                                try {
                                    engine.eval("load('classpath:platypus.js', space);", bindings);
                                } catch (ScriptException ex) {
                                    Logger.getLogger(Scripts.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            // Zombie processing ...
                            Runnable task = queue.poll();
                            while (task != null) {
                                task.run();
                                task = queue.poll();
                            }
                            boolean setted = worker.compareAndSet(thisThread, null);
                            assert setted : "Worker electing assumption failed";// Always successfull CAS.
                        }
                    } while (!queueVersion.compareAndSet(version, newVersion));
                } finally {
                    setSpace(oldSpace);
                }
            });
        }

        protected Object request;
        protected Object response;
        protected Object session;
        protected Object principal;
        protected Integer asyncsCount;

        public Object getRequest() {
            return request;
        }

        public void setRequest(Object aRequest) {
            request = aRequest;
        }

        public Object getResponse() {
            return response;
        }

        public void setResponse(Object aResponse) {
            response = aResponse;
        }

        public Object getSession() {
            return session;
        }

        public void setSession(Object aSession) {
            session = aSession;
        }

        public Object getPrincipal() {
            return principal;
        }

        public void setPrincipal(Object aSession) {
            principal = aSession;
        }

        public int getAsyncsCount() {
            return asyncsCount != null ? asyncsCount : 0;
        }

        public void incAsyncsCount() {
            if (asyncsCount != null) {
                asyncsCount++;
            }
        }

        public void initAsyncs(Integer aSeed) {
            asyncsCount = aSeed;
        }

        public JSObject readJsArray(Collection<Map<String, Object>> aCollection) {
            JSObject result = makeArray();
            JSObject jsPush = (JSObject) result.getMember("push");
            aCollection.forEach((Map<String, Object> aItem) -> {
                JSObject jsItem = makeObj();
                aItem.entrySet().forEach((Map.Entry<String, Object> aItemContent) -> {
                    jsItem.setMember(aItemContent.getKey(), toJs(aItemContent.getValue()));
                });
                jsPush.call(result, new Object[]{jsItem});
            });
            return result;
        }

    }
    protected static Consumer<Runnable> tasks;
    // bio thread pool
    protected static ThreadPoolExecutor bio;

    public static void initTasks(Consumer<Runnable> aTasks) {
        assert tasks == null : "Scripts tasks are already initialized";
        tasks = aTasks;
    }

    public static void offerTask(Runnable aTask) {
        assert tasks != null : "Scripts tasks are not initialized";
        tasks.accept(aTask);
    }

    public static void initBIO(int aMaxThreads) {
        bio = new ThreadPoolExecutor(aMaxThreads, aMaxThreads,
                    1L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    new DeamonThreadFactory("platypus-bio-", false));
        bio.allowCoreThreadTimeOut(true);
    }

    public static void startBIO(Runnable aBlocked) {
        bio.submit(() -> {
            aBlocked.run();
        });
    }

    protected static class Executed {

        protected long timeStamp;
        protected Object scriptResult;

        public Executed(long aTimeStamp, Object aScriptResult) {
            timeStamp = aTimeStamp;
            scriptResult = aScriptResult;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public Object getScriptResult() {
            return scriptResult;
        }
    }

    public static Space createSpace() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Space space = new Space(engine);
        return space;
    }

    public static boolean isInitialized() {
        Space space = getSpace();
        return space != null
                && space.listenElementsFunc != null
                && space.listenFunc != null
                && space.scalarDefFunc != null
                && space.collectionDefFunc != null;
    }

    public static boolean isValidJsIdentifier(final String aName) {
        if (aName != null && !aName.trim().isEmpty()) {
            try {
                FunctionNode astRoot = parseJs(String.format("function %s() {}", aName));
                return astRoot != null && !astRoot.getBody().getStatements().isEmpty();
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    public static FunctionNode parseJs(String aJsContent) {
        Source source = Source.sourceFor("", aJsContent);//NOI18N
        Options options = new Options(null);
        ScriptEnvironment env = new ScriptEnvironment(options, null, null);
        ErrorManager errors = new ErrorManager();
        Parser p = new Parser(env, source, errors);
        return p.parse();
    }

    /**
     * Extracts the comments tokens from a JavaScript source.
     *
     * @param aSource a source
     * @return a list of comment tokens
     */
    public static List<Long> getCommentsTokens(String aSource) {
        TokenStream tokens = new TokenStream();
        Lexer lexer = new Lexer(Source.sourceFor("", aSource), tokens);//NOI18N
        long t;
        TokenType tt = TokenType.EOL;
        int i = 0;
        List<Long> commentsTokens = new ArrayList<>();
        while (tt != TokenType.EOF) {
            // Get next token in nashorn's parser way
            while (i > tokens.last()) {
                if (tokens.isFull()) {
                    tokens.grow();
                }
                lexer.lexify();
            }
            t = tokens.get(i++);
            tt = Token.descType(t);
            if (tt == TokenType.COMMENT) {
                commentsTokens.add(t);
            }
        }
        return commentsTokens;
    }

    /**
     * Removes all commentaries from some JavaScript code.
     *
     * @param text a source
     * @return comments-free JavaScript code
     */
    public static String removeComments(String text) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Long t : getCommentsTokens(text)) {
            int offset = Token.descPosition(t);
            int lenght = Token.descLength(t);
            sb.append(text.substring(i, offset));
            for (int j = 0; j < lenght; j++) {
                sb.append(" ");//NOI18N
            }
            i = offset + lenght;
        }
        sb.append(text.substring(i));
        return sb.toString();
    }

    /**
     * Searches for all <code>this</code> aliases in a constructor.
     *
     * @param moduleConstructor a constructor to search in
     * @return a set of aliases including <code>this</code> itself
     */
    public static Set<String> getThisAliases(final FunctionNode moduleConstructor) {
        final Set<String> aliases = new HashSet<>();
        if (moduleConstructor != null && moduleConstructor.getBody() != null) {
            aliases.add(THIS_KEYWORD);
            LexicalContext lc = new LexicalContext();
            moduleConstructor.accept(new NodeOperatorVisitor<LexicalContext>(lc) {

                @Override
                public boolean enterVarNode(VarNode varNode) {
                    if (lc.getCurrentFunction() == moduleConstructor) {
                        if (varNode.getAssignmentSource() instanceof IdentNode) {
                            IdentNode in = (IdentNode) varNode.getAssignmentSource();
                            if (THIS_KEYWORD.equals(in.getName())) {
                                aliases.add(varNode.getAssignmentDest().getName());
                            }
                        }
                    }
                    return super.enterVarNode(varNode);
                }
            });
        }
        return aliases;
    }

    protected static final String SCRIPT_NOT_INITIALIZED = "Platypus script functions are not initialized.";

    public static void unlisten(JSObject aCookie) {
        JSObject unlisten = (JSObject) aCookie.getMember("unlisten");
        unlisten.call(null, new Object[]{});
    }

    public static boolean isInNode(Node node, int offset) {
        return node.getStart() <= offset
                && offset <= node.getFinish() + 1;
    }

    public static boolean isInNode(Node outerNode, Node innerNode) {
        return outerNode.getStart() <= innerNode.getStart()
                && innerNode.getFinish() <= outerNode.getFinish();
    }

    public static Node getOffsetNode(Node node, final int offset) {
        GetOffsetNodeVisitorSupport vs = new GetOffsetNodeVisitorSupport(node, offset);
        Node offsetNode = vs.getOffsetNode();
        return offsetNode != null ? offsetNode : node;
    }

    private static class GetOffsetNodeVisitorSupport {

        private final Node root;
        private final int offset;
        private Node offsetNode;

        public GetOffsetNodeVisitorSupport(Node root, int offset) {
            this.root = root;
            this.offset = offset;
        }

        public Node getOffsetNode() {
            final LexicalContext lc = new LexicalContext();
            root.accept(new NodeVisitor<LexicalContext>(lc) {

                @Override
                protected boolean enterDefault(Node node) {
                    if (isInNode(node, offset)) {
                        offsetNode = node;
                        return true;
                    }
                    return false;
                }
            });
            return offsetNode;
        }
    }
}
