package me.laravieira.willy.utils;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;

import java.util.Collections;
import java.util.Set;

public class MarkdownTextLinkRenderer extends AbstractVisitor implements NodeRenderer {
    private final TextContentNodeRendererContext context;

    public MarkdownTextLinkRenderer(TextContentNodeRendererContext context) {
        this.context = context;
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>>singleton(Link.class);
    }

    @Override
    public void render(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }
}
