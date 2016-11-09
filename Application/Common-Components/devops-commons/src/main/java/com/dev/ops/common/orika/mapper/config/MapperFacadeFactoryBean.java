/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.dev.ops.common.orika.mapper.config;

import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MapperFacadeFactoryBean implements FactoryBean<MapperFacade> {

	private final Set<MappingConfigurer> configurers;

	public MapperFacadeFactoryBean() {
		super();
		this.configurers = new HashSet<MappingConfigurer>();
	}

	@Autowired(required = true)
	public MapperFacadeFactoryBean(final Set<MappingConfigurer> configurers) {
		super();
		this.configurers = configurers;
	}

	@Override
	public MapperFacade getObject() throws Exception {
		final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().useBuiltinConverters(true).build();

		for(final MappingConfigurer configurer : this.configurers) {
			configurer.configure(mapperFactory);
		}
		return mapperFactory.getMapperFacade();
	}

	@Override
	public Class<?> getObjectType() {
		return MapperFacade.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
