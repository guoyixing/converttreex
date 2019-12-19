package com.gyx.converttreex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 树节点
 */
public class TreeNode<T> {
    /**
     * 数据id
     */
    private Object id;
    /**
     * 数据实体
     */
    private T data;
    /**
     * 直接子节点
     */
    private List<TreeNode<T>> childrenNode = new ArrayList<>();

    /**
     * 追溯父节点Id,有序，从0：root-》finally：parentId
     */
    private List<Object> parentNode = new ArrayList<>();

    /**
     * 同级节点Id
     */
    private List<Object> peerNode = new ArrayList<>();

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<TreeNode<T>> getChildrenNode() {
        return childrenNode;
    }

    public void setChildrenNode(List<TreeNode<T>> childrenNode) {
        this.childrenNode = childrenNode;
    }

    public TreeNode(T data, List<TreeNode<T>> childrenNode) {
        this.data = data;
        this.childrenNode = childrenNode;
    }

    public TreeNode() {
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public List<Object> getParentNode() {
        return parentNode;
    }

    public void setParentNode(List<Object> parentNode) {
        this.parentNode = parentNode;
    }

    public List<Object> getPeerNode() {
        return peerNode;
    }

    public void setPeerNode(List<Object> peerNode) {
        this.peerNode = peerNode;
    }

    /**
     * 获取子节点
     *
     * @param datas   数据集合
     * @param idName  id字段名
     * @param fidName fid字段名
     * @return 子节点集合
     */
    public List<TreeNode<T>> childrenNode(List<T> datas, String idName, String fidName, ConvertTree<T> convertTree) {
        Object idValue = convertTree.getFieldValue(data, idName);
        List<T> collect = datas.stream()
                .filter(date -> idValue.equals(convertTree.getFieldValue(date, fidName)))
                .collect(Collectors.toList());
        //获取所有节点id
        List<Object> childrenNodeId = collect.stream().map(m -> convertTree.getFieldValue(m, idName)).collect(Collectors.toList());
        datas.removeAll(collect);
        for (T node : collect) {
            TreeNode<T> treeNode = new TreeNode<>();
            convertTree.setId(treeNode,node,idName);
            convertTree.setData(treeNode,node);
            convertTree.setPeer(treeNode,childrenNodeId);
            convertTree.setParent(treeNode,initParent());
            childrenNode.add(treeNode);
        }
        return childrenNode;
    }

    public List<Object> initParent(){
        List<Object> copy = new ArrayList<>(parentNode);
        copy.add(id);
        return copy;
    }
}