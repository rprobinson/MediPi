package com.dev.ops.common.orika.mapper.config;

import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component()
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

	@SuppressWarnings("deprecation")
	@Override
	public MapperFacade getObject() throws Exception {
		final MapperFactory mapperFactory = new DefaultMapperFactory.Builder().useBuiltinConverters(true).build();

		for(final MappingConfigurer configurer : this.configurers) {
			configurer.configure(mapperFactory);
		}
		mapperFactory.build();
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
