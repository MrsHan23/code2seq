package JavaExtractor.Visitors;

import JavaExtractor.Common.CommandLineValues;
import JavaExtractor.Common.Common;
import JavaExtractor.Common.MethodContent;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("StringEquality")
public class FunctionVisitor extends VoidVisitorAdapter<Object> {
    private final ArrayList<MethodContent> m_Methods = new ArrayList<>();
    private final CommandLineValues m_CommandLineValues;

    public FunctionVisitor(CommandLineValues commandLineValues) {
        this.m_CommandLineValues = commandLineValues;
    }

    @Override
    public void visit(MethodDeclaration node, Object arg) {
        visitMethod(node);

        super.visit(node, arg);
    }

    /** Get comments in a given method
     * 
     * @param node - a method node
     * @return comments contained in a method
     */
    private List<String> getCommentsMethod(Node node){
        // get all comments contained in node
        List<Comment> comments = node.getAllContainedComments();

        // add comments in lower case to corpus/collection of comments (exclude orphan comments)
        List<String> corpus = new ArrayList<>();
        for (Comment comment: comments){
            // exclude orphan comments
            if (!comment.isOrphan()){
                corpus.add(comment.getContent().toLowerCase());
            }
        }

        // check if node is associated with a comment and add it to corpus/collection of comments
        if (node.getComment() != null){
            corpus.add(node.getComment().getContent().toLowerCase());
        }
        return corpus;
    }

    private void visitMethod(MethodDeclaration node) {
        LeavesCollectorVisitor leavesCollectorVisitor;

        // check if TFIDF is enabled and get collection of comments in current method
        if (m_CommandLineValues.IncludeTFIDF){
            leavesCollectorVisitor = new LeavesCollectorVisitor(m_CommandLineValues , getCommentsMethod(node));
        } else {
            leavesCollectorVisitor = new LeavesCollectorVisitor(m_CommandLineValues , new ArrayList<>());
        }
        
        leavesCollectorVisitor.visitDepthFirst(node);
        ArrayList<Node> leaves = leavesCollectorVisitor.getLeaves();

        String normalizedMethodName = Common.normalizeName(node.getName(), Common.BlankWord);
        ArrayList<String> splitNameParts = Common.splitToSubtokens(node.getName());
        String splitName = normalizedMethodName;
        if (splitNameParts.size() > 0) {
            splitName = String.join(Common.internalSeparator, splitNameParts);
        }

        if (node.getBody() != null) {
            long methodLength = getMethodLength(node.getBody().toString());
            if (m_CommandLineValues.MaxCodeLength > 0) {
                if (methodLength >= m_CommandLineValues.MinCodeLength && methodLength <= m_CommandLineValues.MaxCodeLength) {
                    m_Methods.add(new MethodContent(leaves, splitName));
                }
            } else {
                m_Methods.add(new MethodContent(leaves, splitName));
            }
        }
    }

    private long getMethodLength(String code) {
        String cleanCode = code.replaceAll("\r\n", "\n").replaceAll("\t", " ");
        if (cleanCode.startsWith("{\n"))
            cleanCode = cleanCode.substring(3).trim();
        if (cleanCode.endsWith("\n}"))
            cleanCode = cleanCode.substring(0, cleanCode.length() - 2).trim();
        if (cleanCode.length() == 0) {
            return 0;
        }
        return Arrays.stream(cleanCode.split("\n"))
                .filter(line -> (line.trim() != "{" && line.trim() != "}" && line.trim() != ""))
                .filter(line -> !line.trim().startsWith("/") && !line.trim().startsWith("*")).count();
    }

    public ArrayList<MethodContent> getMethodContents() {
        return m_Methods;
    }
}
