package com.gyx.converttreex;

import java.util.*;
import java.util.function.Consumer;

public class Tree<T> {
    /**
     * 所有数据
     */
    private List<T> datas;
    /**
     * 森林的树根
     */
    private List<TreeNode<T>> treeRoot;

    /**
     * 树的配置
     */
    private ConvertTree<T> convertTree;

    /**
     * 平铺树节点,id与树节点关系
     */
    private Map<Object, TreeNode<T>> idMapTree;

    /**
     * 平铺树节点,data与树节点关系
     */
    private Map<T, TreeNode<T>> tMapTree;

    public Tree(List<T> datas, List<TreeNode<T>> treeRoot, ConvertTree<T> convertTree) {
        this.datas = datas;
        this.treeRoot = treeRoot;
        this.convertTree = convertTree;
        //遍历树
        forForest(treeRoot, this::insertMap);
    }

    private void insertMap(TreeNode<T> treeNode) {
        idMapTree.put(treeNode.getId(), treeNode);
        if (convertTree.getCacheEntity()) {
            tMapTree.put(treeNode.getData(), treeNode);
        }
    }

    private void updateMap(Object oldTreeNodeId, T oldTreeNode, TreeNode<T> newTreeNode) {
        idMapTree.remove(oldTreeNodeId);
        idMapTree.put(newTreeNode.getId(), newTreeNode);
        if (convertTree.getCacheEntity()) {
            tMapTree.remove(oldTreeNode);
            tMapTree.put(newTreeNode.getData(), newTreeNode);
        }
    }

    private void deleteMap(TreeNode<T> treeNode) {
        List<TreeNode<T>> childrenNode = treeNode.getChildrenNode();
        if (childrenNode.size() == 0) {
            return;
        }
        idMapTree.remove(treeNode.getId());
        if (convertTree.getCacheEntity()) {
            tMapTree.remove(treeNode.getData());
        }
        for (TreeNode<T> tTreeNode : childrenNode) {
            deleteMap(tTreeNode);
        }
    }

    private void forForest(List<TreeNode<T>> forest, Consumer<TreeNode<T>> t) {
        for (TreeNode<T> treeRoot : forest) {
            forTree(treeRoot, t);
        }
    }

    /**
     * 先序遍历，并且对每个节点进行统一操作
     */
    public void forTree(TreeNode<T> treeRoot, Consumer<TreeNode<T>> t) {
        TreeNode<T> tree = treeRoot;
        Stack<TreeNode<T>> stack = new Stack<>();
        //如果x不为空放入栈中
        if (tree != null) {
            stack.push(tree);
        }
        //对栈进行循环操作
        while (!stack.empty()) {
            //访问栈顶元素
            tree = stack.pop();
            //操作元素
            t.accept(tree);
            //所有子节点放入栈
            for (TreeNode<T> children : tree.getChildrenNode()) {
                stack.push(children);
            }
        }
    }

    /**
     * 根据id获取树
     */
    public TreeNode<T> getNodeById(Object id) {
        return idMapTree.get(id);
    }

    /**
     * 根据ids获取treeList
     */
    public List<TreeNode<T>> getNodeList(List<Object> ids) {
        List<TreeNode<T>> treeNodes = new ArrayList<>();
        for (Object id : ids) {
            treeNodes.add(idMapTree.get(id));
        }
        return treeNodes;
    }

    /**
     * 插入子节点
     */
    public TreeNode<T> insertTreeNode(TreeNode<T> parentNode, T data) {
        TreeNode<T> treeNode = new TreeNode<>();
        convertTree.setId(treeNode, data, convertTree.getIdName());
        convertTree.setData(treeNode, data);
        convertTree.setParent(treeNode, treeNode.initParent());
        //更新子节点的同级id
        for (TreeNode<T> tTreeNode : parentNode.getChildrenNode()) {
            tTreeNode.getPeerNode().add(treeNode.getId());
            if (treeNode.getPeerNode().size() == 0) {
                convertTree.setPeer(treeNode, tTreeNode.getPeerNode());
            }
        }
        //添加的是叶子节点时，设置同级id
        if (treeNode.getPeerNode().size() == 0) {
            convertTree.setPeer(treeNode, Collections.singletonList(treeNode.getId()));
        }
        //更新映射
        insertMap(treeNode);
        return treeNode;
    }

    /**
     * 删除节点根据id
     */
    public void deleteTreeNode(Object treeNodeId) {
        TreeNode<T> tTreeNode = idMapTree.get(treeNodeId);
        deleteTreeNode(tTreeNode);
    }

    /**
     * 删除节点
     */
    public void deleteTreeNode(TreeNode<T> treeNode) {
        //处理父节点引用
        List<Object> parentNodes = treeNode.getParentNode();
        Object parentId = parentNodes.get(parentNodes.size() - 1);
        TreeNode<T> parentNode = this.getNodeById(parentId);
        //删除节点
        parentNode.getChildrenNode().remove(treeNode);
        //更新同级节点
        List<TreeNode<T>> peerNode = parentNode.getChildrenNode();
        peerNode.forEach(m -> m.getPeerNode().remove(treeNode.getId()));
        //更新映射
        deleteMap(treeNode);
    }

    /**
     * 修改节点内容
     */
    public TreeNode<T> updateTreeNode(Object oldNodeId, TreeNode<T> newNode) {
        TreeNode<T> oldNode = idMapTree.get(oldNodeId);
        T oldNodeData = oldNode.getData();
        //节点参数
        oldNode.setId(newNode.getId());
        oldNode.setData(newNode.getData());
        //修改同级节点的id
        List<Object> parentNodes = oldNode.getParentNode();
        TreeNode<T> parentNode = idMapTree.get(parentNodes.get(parentNodes.size() - 1));
        for (TreeNode<T> tTreeNode : parentNode.getChildrenNode()) {
            tTreeNode.getPeerNode().remove(oldNodeId);
            tTreeNode.getPeerNode().add(newNode.getId());
        }

        //修改子节点的追溯
        forTree(oldNode, m -> {
            List<Object> parent = m.getParentNode();
            int oldNodeIndex = parent.indexOf(oldNodeId);
            if (oldNodeIndex >= 0) {
                parent.set(oldNodeIndex, newNode.getId());
            }
        });
        //更新映射
        updateMap(oldNodeId, oldNodeData, oldNode);
        return oldNode;
    }

    /**
     * 根据data获取树
     */
    public TreeNode<T> getTreeNode(T data) {
        if (convertTree.getCacheEntity()) {
            return null;
        }
        return tMapTree.get(data);
    }


    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public List<TreeNode<T>> getTreeRoot() {
        return treeRoot;
    }

    public void setTreeRoot(List<TreeNode<T>> treeRoot) {
        this.treeRoot = treeRoot;
    }

    public ConvertTree<T> getConvertTree() {
        return convertTree;
    }

    public void setConvertTree(ConvertTree<T> convertTree) {
        this.convertTree = convertTree;
    }

    public Map<Object, TreeNode<T>> getIdMapTree() {
        return idMapTree;
    }

    public void setIdMapTree(Map<Object, TreeNode<T>> idMapTree) {
        this.idMapTree = idMapTree;
    }

    public Map<T, TreeNode<T>> gettMapTree() {
        return tMapTree;
    }

    public void settMapTree(Map<T, TreeNode<T>> tMapTree) {
        this.tMapTree = tMapTree;
    }
}
