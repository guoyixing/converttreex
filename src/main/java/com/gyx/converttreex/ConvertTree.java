package com.gyx.converttreex;


import com.gyx.converttreex.interfaces.TreeFid;
import com.gyx.converttreex.interfaces.TreeId;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 将数据生成树状结构
 */
public class ConvertTree<T> {
    /**
     * 是否缓存节点实例
     */
    private Boolean isCacheEntity;
    /**
     * 是否缓存追溯父节点id
     */
    private Boolean isCacheParent;
    /**
     * 是否缓存同级节点id
     */
    private Boolean isCachePeer;

    private String idName;

    public ConvertTree(Boolean isCacheEntity, Boolean isCacheParent, Boolean isCachePeer) {
        this.isCacheEntity = isCacheEntity;
        this.isCacheParent = isCacheParent;
        this.isCachePeer = isCachePeer;
    }

    /**
     * 形成森林
     */
    public Tree<T> getForest(List<T> datas, String idName, String fidName) {
        this.idName = idName;
        List<TreeNode<T>> forest = new ArrayList<>();
        while (!datas.isEmpty()) {
            TreeNode<T> tree = getTree(datas, idName, fidName);
            forest.add(tree);
        }
        return new Tree<>(datas,forest,this);
    }

    /**
     * 形成森林(使用注解)
     */
    public Tree<T> getForest(List<T> datas) {
        //获取idname和fidName
        String idName = null;
        String fidName = null;
        if (!datas.isEmpty()) {
            //得到class
            Class cls = datas.get(0).getClass();
            //得到所有属性
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                TreeId treeId = field.getAnnotation(TreeId.class);
                if (treeId != null) {
                    idName = field.getName();
                    this.idName = idName;
                }
                TreeFid treeFid = field.getAnnotation(TreeFid.class);
                if (treeFid != null) {
                    fidName = field.getName();
                }
            }
        }

        return getForest(datas, idName, fidName);
    }

    /**
     * 形成树
     */
    private TreeNode<T> getTree(List<T> datas, String idName, String fidName) {
        //获取树根
        TreeNode<T> rootNode = getRootNode(datas, idName, fidName);
        //遍历树根的子集
        List<TreeNode<T>> childrenNode = rootNode.getChildrenNode();
        forChildren(datas, idName, fidName, childrenNode);
        //此时树已经形成
        return rootNode;
    }

    /**
     * 递归遍历子节点
     */
    private void forChildren(List<T> datas, String idName, String fidName, List<TreeNode<T>> childrenNode) {
        //需要遍历的集合
        List<TreeNode<T>> needForList = new ArrayList<>();
        for (TreeNode<T> tTreeNode : childrenNode) {
            List<TreeNode<T>> treeNodes = tTreeNode.childrenNode(datas, idName, fidName, this);
            needForList.addAll(treeNodes);
        }
        if (!needForList.isEmpty()) {
            forChildren(datas, idName, fidName, needForList);
        }
    }

    /**
     * 获取根节点
     *
     * @param datas
     * @param idName
     * @param fidName
     * @return
     */
    private TreeNode<T> getRootNode(List<T> datas, String idName, String fidName) {
        if (datas.isEmpty()) {
            return null;
        }
        T node = datas.get(0);
        //获取根节点
        T rootNode = getRootNode(datas, idName, fidName, node);
        TreeNode<T> rootTreeNode = new TreeNode<>();
        datas.remove(rootNode);
        //设置id
        setId(rootTreeNode, rootNode, idName);
        //设置data
        setData(rootTreeNode, rootNode);
        //获取子节点
        rootTreeNode.childrenNode(datas, idName, fidName, this);
        return rootTreeNode;
    }

    /**
     * 递归获取根节点
     *
     * @param datas
     * @param idName
     * @param fidName
     * @param node
     * @return
     */
    private T getRootNode(List<T> datas, String idName, String fidName, T node) {
        T fNode = null;
        Object fieldValue = getFieldValue(node, fidName);
        for (T data : datas) {
            if (getFieldValue(data, idName).equals(fieldValue)) {
                fNode = data;
                break;
            }
        }
        if (fNode == null) {
            return node;
        } else {
            return getRootNode(datas, idName, fidName, fNode);
        }
    }


    /**
     * 获取字段值
     *
     * @param o         class
     * @param fieldName 字段名
     * @return String
     */
    public Object getFieldValue(T o, String fieldName) {
        //得到class
        Class cls = o.getClass();
        //得到所有属性
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            try {
                //打开私有访问
                field.setAccessible(true);
                //获取属性
                if (field.getName().equals(fieldName)) {
                    Object result = field.get(o);
                    if (result == null) {
                        return null;
                    }
                    return result;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("获取属性值失败");
    }

    /**
     * 设置节点id
     */
    public void setId(TreeNode<T> rootTreeNode, T rootNode, String idName) {
        //获取节点id
        Object id = getFieldValue(rootNode, idName);
        rootTreeNode.setId(id);
    }

    /**
     * 设置节点数据
     */
    public void setData(TreeNode<T> rootTreeNode, T rootNode) {
        if (isCacheEntity) {
            rootTreeNode.setData(rootNode);
        }
    }

    /**
     * 设置同级节点
     */
    public void setPeer(TreeNode<T> rootTreeNode, List<Object> peerrenNodeId) {
        if (isCachePeer) {
            rootTreeNode.setPeerNode(peerrenNodeId);
        }
    }

    /**
     * 设置追溯父节点
     */
    public void setParent(TreeNode<T> rootTreeNode, List<Object> parentNodeId) {
        if (isCacheParent) {
            rootTreeNode.setParentNode(parentNodeId);
        }
    }

    public Boolean getCacheEntity() {
        return isCacheEntity;
    }

    public Boolean getCacheParent() {
        return isCacheParent;
    }

    public Boolean getCachePeer() {
        return isCachePeer;
    }

    public String getIdName() {
        return idName;
    }
}