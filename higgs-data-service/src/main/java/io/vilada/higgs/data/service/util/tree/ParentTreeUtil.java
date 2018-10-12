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
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author caiyunpeng
 * parentId 树类型 通用方法
 * @param <T>  树类型 需实现IParentType接口
 * @param 
 */
public class ParentTreeUtil<T extends IParentType> {
	
	public List<T> getSub(List<T> list ,String rootId){
		Set<String> keySet = new TreeSet<String>();
		List<T> sub = new ArrayList<T>();
		for(int i = 0 ;i < list.size();i++){
			T item = list.get(i);
			String parentId=item.getParentId();
			String id = item.getId();
			if(id.equals(rootId)){
				sub.add(item);
			}
			if(keySet.contains(parentId) || parentId.equals(rootId)){
				sub.add(item);
				keySet.add(id);
			}
		}
		return sub;
	}
}
