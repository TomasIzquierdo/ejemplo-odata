package com.cairone.odataexample.datasources;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.dtos.PaisFrmDto;
import com.cairone.odataexample.dtos.ProvinciaFrmDto;
import com.cairone.odataexample.entities.ProvinciaPKEntity;
import com.cairone.odataexample.dtos.validators.PaisFrmDtoValidator;
import com.cairone.odataexample.dtos.validators.ProvinciaFrmDtoValidator;
import com.cairone.odataexample.edm.resources.PaisEdm;
import com.cairone.odataexample.edm.resources.ProvinciaEdm;
import com.cairone.odataexample.entities.PaisEntity;
import com.cairone.odataexample.entities.ProvinciaEntity;
import com.cairone.odataexample.repositories.PaisRepository;
import com.cairone.odataexample.repositories.ProvinciaRepository;
import com.cairone.odataexample.strategyBuilders.PaisesStrategyBuilder;
import com.cairone.odataexample.strategyBuilders.ProvinciasStrategyBuilder;
import com.mysema.query.types.expr.BooleanExpression;
import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.ODataSystemException;
import com.sdl.odata.api.edm.model.EntityDataModel;
import com.sdl.odata.api.edm.util.EdmUtil;
import com.sdl.odata.api.parser.ODataUri;
import com.sdl.odata.api.parser.ODataUriUtil;
import com.sdl.odata.api.parser.TargetType;
import com.sdl.odata.api.processor.datasource.DataSource;
import com.sdl.odata.api.processor.datasource.DataSourceProvider;
import com.sdl.odata.api.processor.datasource.ODataDataSourceException;
import com.sdl.odata.api.processor.datasource.TransactionalDataSource;
import com.sdl.odata.api.processor.link.ODataLink;
import com.sdl.odata.api.processor.query.QueryOperation;
import com.sdl.odata.api.processor.query.QueryResult;
import com.sdl.odata.api.processor.query.strategy.QueryOperationStrategy;
import com.sdl.odata.api.service.ODataRequestContext;

import scala.Option;

import org.springframework.validation.DataBinder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Component
public class ProvinciaDataSource implements DataSourceProvider, DataSource {

	@Autowired private ProvinciaRepository provinciaRepository = null;
	@Autowired private PaisRepository paisRepository = null;
	@Autowired private ProvinciaFrmDtoValidator provinciaFrmDtoValidator = null;
	
	@Autowired
	private MessageSource messageSource = null;	
	
	@Override
	public Object create(ODataUri uri, Object entity, EntityDataModel entityDataModel) throws ODataException {

		if(entity instanceof ProvinciaEdm) {
			
			ProvinciaEdm provinciaEdm = (ProvinciaEdm) entity;
			
			
			//aca empieza la joda
    		ProvinciaFrmDto provinciaFrmDto = new ProvinciaFrmDto(provinciaEdm);

    		DataBinder binder = new DataBinder(provinciaFrmDto);
			
			binder.setValidator(provinciaFrmDtoValidator);
			binder.validate();
			
			BindingResult bindingResult = binder.getBindingResult();
			
			if(bindingResult.hasFieldErrors()) {
				
				for (Object object : bindingResult.getAllErrors()) {
				    if(object instanceof FieldError) {
				        FieldError fieldError = (FieldError) object;
				        String message = messageSource.getMessage(fieldError, null);
				        throw new ODataDataSourceException(
				        		String.format("HAY DATOS INVALIDOS EN LA SOLICITUD ENVIADA. %s", message));
				    }
				}
			}
			
			ProvinciaEntity provinciaEntity = new ProvinciaEntity();
			//PaisEntity paisEntity = new PaisEntity();


			provinciaEntity.setId(provinciaFrmDto.getId());
			provinciaEntity.setNombre(provinciaFrmDto.getNombre());
			provinciaEntity.setPais(paisRepository.findOne((provinciaFrmDto.getPaisID())));
    		
			provinciaRepository.save(provinciaEntity);			
			
    		return provinciaEdm;
		}
		
		throw new ODataDataSourceException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PROVINCIA");
	}
	
	
	

	@Override
	public Object update(ODataUri uri, Object entity, EntityDataModel entityDataModel) throws ODataException {
		// TODO Auto-generated method stub
		
    	if(entity instanceof ProvinciaEdm) {
    		
    		Map<String, Object> oDataUriKeyValues = ODataUriUtil.asJavaMap(ODataUriUtil.getEntityKeyMap(uri, entityDataModel));
    		
    		ProvinciaEdm provincia = (ProvinciaEdm) entity;
    		
    		oDataUriKeyValues.values().forEach(item -> {
    			provincia.setId(Integer.valueOf( item.toString() ));
    		});
    		
    		ProvinciaFrmDto provinciaFrmDto = new ProvinciaFrmDto(provincia);

    		DataBinder binder = new DataBinder(provinciaFrmDto);
			
			binder.setValidator(provinciaFrmDtoValidator);
			binder.validate();
			
			BindingResult bindingResult = binder.getBindingResult();

			if(bindingResult.hasFieldErrors()) {
				
				for (Object object : bindingResult.getAllErrors()) {
				    if(object instanceof FieldError) {
				        FieldError fieldError = (FieldError) object;
				        String message = messageSource.getMessage(fieldError, null);
				        throw new ODataDataSourceException(
				        		String.format("HAY DATOS INVALIDOS EN LA SOLICITUD ENVIADA. %s", message));
				    }
				}
			}
			
        	//Integer paisID = provincia.getPaisId();
        	//PaisEntity paisEntity = paisRepository.findOne(paisID);
			
        	Integer provinciaID = provincia.getId();
        	
        	ProvinciaPKEntity provinciaPKEntity = new ProvinciaPKEntity();
        	//provinciaPKEntity.setPaisId(provincia.getPais().getId());
        	provinciaPKEntity.setPaisId(provincia.getPaisId());
        	provinciaPKEntity.setProvinciaId(provinciaID);
        	ProvinciaEntity provinciaEntity =  provinciaRepository.findOne(provinciaPKEntity);             //  findOne(provinciaID);
        	//tengo una duda aca tengo que buscar una provincia
        	
    		if(provinciaEntity == null) {
    			throw new ODataDataSourceException(String.format("NO SE ENCUENTRA UNA PROVINCIA CON ID %s", provincia.getId()));
    		}
    		
    		provinciaEntity.setNombre(provinciaFrmDto.getNombre());
    		
    		//provinciaEntity.setPais(paisRepository.findOne((provinciaFrmDto.getPaisID())));
    		//tengo duda
    		
    		provinciaRepository.save(provinciaEntity);
    		
    		return new ProvinciaEdm(provinciaEntity);
    	}
    	
    	throw new ODataDataSourceException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PAIS");		
		
		
//		return null;
	}

	@Override
	public void delete(ODataUri uri, EntityDataModel entityDataModel) throws ODataException {
		// TODO Auto-generated method stub
		Option<Object> entity = ODataUriUtil.extractEntityWithKeys(uri, entityDataModel);
	    	
	    	if(entity.isDefined()) {
	    		
	    		ProvinciaEdm provincia = (ProvinciaEdm) entity.get();
	    		

	    		Integer provinciaID = provincia.getId();
	        	ProvinciaPKEntity provinciaPKEntity = new ProvinciaPKEntity();
	        	
	        	//provinciaPKEntity.setPaisId(provincia.getPais().getId());
	        	provinciaPKEntity.setPaisId(provincia.getPaisId());
	        	provinciaPKEntity.setProvinciaId(provinciaID);
	        	ProvinciaEntity provinciaEntity = provinciaRepository.findOne(provinciaPKEntity);   	        	
	    		if(provinciaEntity == null) {
	    			throw new ODataDataSourceException(String.format("NO SE ENCUENTRA UNA PROVINCIA CON ID %s", provincia.getId()));
	    		}
	    		
	    		provinciaRepository.delete(provinciaEntity);
	    		
	    		return;
	    	 }
	    	
	    	throw new ODataDataSourceException("LOS DATOS NO CORRESPONDEN A LA ENTIDAD PROVINCIA");		
	}

	@Override
	public void createLink(ODataUri uri, ODataLink link, EntityDataModel entityDataModel) throws ODataException {
		// NO HACER NADA
	}

	@Override
	public void deleteLink(ODataUri uri, ODataLink link, EntityDataModel entityDataModel) throws ODataException {
		// NO HACER NADA
	}

	@Override
	public TransactionalDataSource startTransaction() {
		throw new ODataSystemException("No support for transactions");
	}

	@Override
	public boolean isSuitableFor(ODataRequestContext requestContext, String entityType) throws ODataDataSourceException {
		return requestContext.getEntityDataModel().getType(entityType).getJavaType().equals(ProvinciaEdm.class);
	}

	@Override
	public DataSource getDataSource(ODataRequestContext requestContext) {
		return this;
	}

	@Override
	public QueryOperationStrategy getStrategy(ODataRequestContext requestContext, QueryOperation operation, TargetType expectedODataEntityType) throws ODataException {
		// TODO Auto-generated method stub
		//return null;
		ProvinciasStrategyBuilder builder = new ProvinciasStrategyBuilder();
		BooleanExpression expression = builder.buildCriteria(operation, requestContext);
		List<Sort.Order> orderByList = builder.getOrderByList();
		
		int limit = builder.getLimit();
        int skip = builder.getSkip();
		List<String> propertyNames = builder.getPropertyNames();
		
		Page<ProvinciaEntity> pageProvinciaEntity = orderByList == null || orderByList.size() == 0 ?
				provinciaRepository.findAll(expression, new PageRequest(0, limit)) :
				provinciaRepository.findAll(expression, new PageRequest(0, limit, new Sort(orderByList)));
		
		List<ProvinciaEntity> provinciaEntities = pageProvinciaEntity.getContent();
		
		return () -> {

			List<ProvinciaEdm> filtered = provinciaEntities.stream().map(entity -> { return new ProvinciaEdm(entity); }).collect(Collectors.toList());
			
			long count = 0;
        	
            if (builder.isCount() || builder.includeCount()) {
                count = filtered.size();

                if (builder.isCount()) {
                    return QueryResult.from(count);
                }
            }

            if (skip != 0 || limit != Integer.MAX_VALUE) {
                filtered = filtered.stream().skip(skip).collect(Collectors.toList());
            }

            if (propertyNames != null && !propertyNames.isEmpty()) {
                try {
                    return QueryResult.from(EdmUtil.getEdmPropertyValue(filtered.get(0), propertyNames.get(0)));
                } catch (IllegalAccessException e) {
                    return QueryResult.from(Collections.emptyList());
                }
            }
            
            QueryResult result = QueryResult.from(filtered);
            if (builder.includeCount()) {
                result = result.withCount(count);
            }
            return result;
		};		
	}

}
