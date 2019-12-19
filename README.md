# converttreex
权限树通用解决方案（增强）
将层级关系的表的数据转换成树
**仅支持java8及以上，id不可重复**
## 1.创建树
### 1.1普通使用
```
ConvertTree<T> convertTree = new ConvertTree<>();
Tree<T> result= convertTree.getForest(数据List，id字段名，父id字段名);
```
### 1.2注解使用
将@TreeId和@TreeFid分别注解在T的id字段和父id字段上 
```
ConvertTree<T> convertTree = new ConvertTree<>();
Tree<T> result= convertTree.getForest(数据List);
```

## 2.树的CRUD
### 2.1查询节点
```
ConvertTree<T> convertTree = new ConvertTree<>();
Tree<T> result= convertTree.getForest(数据List);
//根据id获取节点
result.getNodeById(Object 节点的id);
//根据多个id获取节点列表
result.getNodeList(List<Object> 节点id列表);
//根据节点参数获取树节点
result.getTreeNode(T 节点数据)
```
### 2.2增加节点
```
ConvertTree<T> convertTree = new ConvertTree<>();
Tree<T> result= convertTree.getForest(数据List);
result.insertTreeNode(TreeNode<T> 父节点, T 新增节点数据);
```
### 2.3删除节点
**注意：删除节点后，此节点的子节点会被一起删除**
```
ConvertTree<T> convertTree = new ConvertTree<>();
Tree<T> result= convertTree.getForest(数据List);
//根据id删除节点
result.deleteTreeNode(Object 需要删除节点的id);
//删除指定树节点
result.deleteTreeNode(TreeNode<T> 需要删除的节点);
```
### 2.4修改节点
**注意：如果需要添加子节点或者删除子节点，请使用上面的方法**
**注意：调用修改节点的方法之后，请使用方法返回的节点**
```
ConvertTree<T> convertTree = new ConvertTree<>();
Tree<T> result= convertTree.getForest(数据List);
//根据id修改节点
result.updateTreeNode(Object 旧节点的id,TreeNode<T> 新的节点);
```
### 2.5节点的统一操作
```
ConvertTree<T> convertTree = new ConvertTree<>();
Tree<T> result= convertTree.getForest(数据List);
//以当前节点为树根，进行统一操作
result.forTree(TreeNode<T> 节点, Consumer<TreeNode<T>> lambda表达式);
//例如：获取这个节点下的所有子节点（包括此节点）
List<TreeNode<T>> node = new ArrayList<>();
result.forTree(treeRoot, node::add);
```

## 3.树节点的操作
**注意：修改节点数据后，需要调用修改节点的方法**
### 3.1获取节点id
```
treeNode.getId();
```
### 3.2获取节点数据
```
treeNode.getData();
```
### 3.3获取子节点
```
treeNode.getChildrenNode();
```
### 3.4追溯父节点Id
**注意：父节点id有序，0：root-》finally：parentId**
```
treeNode.getParentNode();
```

### 3.5 获取同级节点Id
**注意：父节点id有序，0：root-》finally：parentId**
```
treeNode.getPeerNode();
```
