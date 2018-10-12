/*
 * Copyright 2018 The Higgs Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vilada.higgs.data.service.util.tree;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author caiyunpeng
 * 树类型转换，将parentId类型的树结构 转换为 children类型的树结构
 * @param <T>  转换目标类型 需实现IChildrenType接口
 * @param <M>  转换源类型  需实现IParentType 接口
 */
@Slf4j
public class TreeConvertor<T extends IChildrenType<T>,M extends IParentType> {

	private Class<T> cls;
	public TreeConvertor(Class<T> cls){
		this.cls = cls;
	}
		
	public List<T> convert(List<M> oldTree) {
		List<T> newTree = new ArrayList<T>();
		List<M> roots = getRootParentIds(oldTree);
		for (int i = 0; i < roots.size(); i++) {
			T node = buildChild(oldTree, roots.get(i));
			newTree.add(node);
		}
		return newTree;
	}

	private List<M> getRootParentIds(List<M> oldTree) {
		List<M> roots = new ArrayList<M>();
		for (int i = 0; i < oldTree.size(); i++) {
			IParentType node = oldTree.get(i);
			if ("-1".equals(node.getParentId())) {
				roots.add(oldTree.get(i));
			}
		}
		return roots;
	}
	
	public T buildChild(List<M> list, String rootId) {
		M rootNode = null;
		for(int i = 0 ;i < list.size();i++){
			if(list.get(i).getId().equals(rootId)){
				rootNode = list.get(i);
				return buildChild(list,rootNode);
			}
		}
		return null;
	}

	public T buildChild(List<M> list, M rootNode) {
		T newTree = null;
		try {
			newTree = cls.newInstance();
			BeanUtils.copyProperties(rootNode, newTree);
		} catch (InstantiationException | IllegalAccessException e) {
			log.warn("init tree failed!",e);
			return null;
		}
		for (int i = 0; i < list.size(); i++) {
			M node = list.get(i);			
			if (node.getParentId().equals(rootNode.getId())) {
				addNode(newTree, list, list.get(i));
				//list.remove(i);
			}
		}
		return newTree;
	}

	private void addNode(T newTree, List<M> list, M old) {
		T child = buildChild(list, old);
		if (child != null) {
			newTree.getChildren().add(child);
		}
	}
}
