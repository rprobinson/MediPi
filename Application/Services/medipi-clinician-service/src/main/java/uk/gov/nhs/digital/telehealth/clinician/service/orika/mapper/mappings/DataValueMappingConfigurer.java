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
package uk.gov.nhs.digital.telehealth.clinician.service.orika.mapper.mappings;

import java.util.List;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;

import org.springframework.stereotype.Component;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.DataValue;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.DataValueEntity;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceAttributeMaster;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceDataMaster;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceMaster;

import com.dev.ops.common.orika.mapper.config.MappingConfigurer;

@Component
public class DataValueMappingConfigurer implements MappingConfigurer {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void configure(final MapperFactory factory) {
		/*factory.classMap((Class<List<RecordingDeviceDataMaster>>) (Class) List.class, (Class<List<DataValue>>) (Class) List.class).customize(new CustomMapper<List<RecordingDeviceDataMaster>, List<DataValue>>() {
			@Override
			public void mapAtoB(final List<RecordingDeviceDataMaster> deviceDataList, final List<DataValue> dataValues, final MappingContext context) {
				for(RecordingDeviceDataMaster deviceData : deviceDataList) {
					RecordingDeviceAttributeMaster deviceAttribute = deviceData.getRecordingDeviceAttribute();
					RecordingDeviceMaster recordingDevice = deviceAttribute.getRecordingDevice();

					DataValue dataValue = new DataValue(recordingDevice.getType(), recordingDevice.getSubtype());
					if(!dataValues.contains(dataValue)) {
						dataValue.setDataTime(deviceData.getDataValueTime());
						dataValue.setSubmittedTime(deviceData.getSubmittedTime());
						dataValue.setReadingType(recordingDevice.getType());
						dataValue.setDevice(recordingDevice.getSubtype());
						dataValues.add(dataValue);

					} else {
						dataValue = dataValues.get(dataValues.indexOf(dataValue));
					}
					dataValue.setData(dataValue.getData() + deviceAttribute.getAttributeName() + ":" + deviceData.getDataValue() + " ");
				}
			}
		}).byDefault().register();

		factory.classMap((Class<List<DataValueEntity>>) (Class) List.class, (Class<List<DataValue>>) (Class) List.class).customize(new CustomMapper<List<DataValueEntity>, List<DataValue>>() {
			@Override
			public void mapAtoB(final List<DataValueEntity> dataValueEntities, final List<DataValue> dataValues, final MappingContext context) {
				for(DataValueEntity dataValueEntity : dataValueEntities) {
					DataValue dataValue = new DataValue(dataValueEntity.getReadingType(), dataValueEntity.getDevice());
					if(!dataValues.contains(dataValue)) {
						dataValue.setDataTime(dataValueEntity.getDataTime());
						dataValue.setSubmittedTime(dataValueEntity.getSubmittedTime());
						dataValue.setReadingType(dataValueEntity.getReadingType());
						dataValue.setDevice(dataValueEntity.getDevice());
						dataValues.add(dataValue);

					} else {
						dataValue = dataValues.get(dataValues.indexOf(dataValue));
					}
					dataValue.setData(dataValue.getData() + dataValueEntity.getAttributeName() + ":" + dataValueEntity.getData() + " ");
				}
			}
		}).byDefault().register();*/

		factory.classMap(List.class, List.class).customize(new CustomMapper<List, List>() {
			@Override
			public void mapAtoB(final List list, final List dataValues, final MappingContext context) {
				if(!list.isEmpty() && list.get(0) instanceof RecordingDeviceDataMaster) {
					List<RecordingDeviceDataMaster> deviceDataList = list;
					for(RecordingDeviceDataMaster deviceData : deviceDataList) {
						RecordingDeviceAttributeMaster deviceAttribute = deviceData.getRecordingDeviceAttribute();
						RecordingDeviceMaster recordingDevice = deviceAttribute.getRecordingDevice();

						DataValue dataValue = new DataValue(recordingDevice.getType(), recordingDevice.getDisplayName());
						if(!dataValues.contains(dataValue)) {
							dataValue.setDataTime(deviceData.getDataValueTime());
							dataValue.setSubmittedTime(deviceData.getSubmittedTime());
							dataValue.setReadingType(recordingDevice.getType());
							dataValue.setDevice(recordingDevice.getDisplayName());
							dataValues.add(dataValue);

						} else {
							dataValue = (DataValue) dataValues.get(dataValues.indexOf(dataValue));
						}
						dataValue.setData(dataValue.getData() + deviceAttribute.getAttributeName() + ":" + deviceData.getDataValue() + " ");
					}
				} else if(!list.isEmpty() && list.get(0) instanceof DataValueEntity) {
					List<DataValueEntity> dataValueEntities = list;
					for(DataValueEntity dataValueEntity : dataValueEntities) {
						DataValue dataValue = new DataValue(dataValueEntity.getReadingType(), dataValueEntity.getDevice());
						if(!dataValues.contains(dataValue)) {
							dataValue.setDataTime(dataValueEntity.getDataTime());
							dataValue.setSubmittedTime(dataValueEntity.getSubmittedTime());
							dataValue.setReadingType(dataValueEntity.getReadingType());
							dataValue.setDevice(dataValueEntity.getDevice());
							dataValues.add(dataValue);

						} else {
							dataValue = (DataValue) dataValues.get(dataValues.indexOf(dataValue));
						}
						dataValue.setData(dataValue.getData() + dataValueEntity.getAttributeName() + ":" + dataValueEntity.getData() + " ");
					}
				}
			}
		}).byDefault().register();
	}
}