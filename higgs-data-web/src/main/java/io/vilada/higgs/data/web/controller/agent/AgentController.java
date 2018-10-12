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

package io.vilada.higgs.data.web.controller.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.elasticsearch.common.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.vilada.higgs.data.meta.bo.in.ApdexForAppInBO;
import io.vilada.higgs.data.meta.bo.in.ApdexForInstanceInBO;
import io.vilada.higgs.data.meta.bo.in.ApdexForTransaction;
import io.vilada.higgs.data.meta.bo.in.ApdexForTransactionBO;
import io.vilada.higgs.data.meta.bo.in.BatchUpdateForTierBO;
import io.vilada.higgs.data.meta.bo.in.TierCreationBO;
import io.vilada.higgs.data.meta.bo.in.WebAgentConfigurationUpdateInBO;
import io.vilada.higgs.data.meta.bo.out.ApdexForAppOutBO;
import io.vilada.higgs.data.meta.bo.out.ApdexForInstanceOutBO;
import io.vilada.higgs.data.meta.bo.out.ApplicationHierarchyListBO;
import io.vilada.higgs.data.meta.bo.out.TierHierarchyListBO;
import io.vilada.higgs.data.meta.dao.entity.newpackage.ApplicationConfiguration;
import io.vilada.higgs.data.meta.dao.entity.newpackage.Instance;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;
import io.vilada.higgs.data.meta.enums.AgentConfigTypeEnum;
import io.vilada.higgs.data.meta.enums.newpackage.ConfigurationTypeEnum;
import io.vilada.higgs.data.meta.enums.newpackage.TierTypeEnum;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.web.util.AgentPackageGenerator;
import io.vilada.higgs.data.web.util.FrontValidationUtils;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.ResultVO;
import io.vilada.higgs.data.web.vo.enums.VoMessageEnum;
import io.vilada.higgs.data.web.vo.factory.ResultVoFactory;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.AppIdInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.ApdexForAppVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.ApdexForInstanceVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.ApdexForTransactionVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.ApplicationConfigurationInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.ApplicationConfigurationPackInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.ApplicationIdAndKeyInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.BatchUpdateForTierVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.DeletionVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.InstanceConfigurationInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.InstanceConfigurationPackInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.InstanceEnabledUpdateInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.InstanceNameUpdateInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.TierCreationInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.TierNameUpdateInVO;
import io.vilada.higgs.data.web.vo.in.management.newpackage.WebAgentConfigurationUpdateInVO;
import io.vilada.higgs.data.web.vo.in.v2.application.ApplicationCreateInVO;
import io.vilada.higgs.data.web.vo.out.agent.ApdexForInstanceOutVO;
import io.vilada.higgs.data.web.vo.out.management.SystemConfigOutVO;
import io.vilada.higgs.data.web.vo.out.management.newpackage.AgentConfigurationOutVO;
import io.vilada.higgs.data.web.vo.out.management.newpackage.ApplicationConfigurationOutVO;
import io.vilada.higgs.data.web.vo.out.management.newpackage.ApplicationHierarchyListOutVO;
import io.vilada.higgs.data.web.vo.out.management.newpackage.ApplicationHierarchyOutVO;
import io.vilada.higgs.data.web.vo.out.management.newpackage.InstanceConfigurationOutVO;
import io.vilada.higgs.data.web.vo.out.management.newpackage.InstanceOutVO;
import io.vilada.higgs.data.web.vo.out.management.newpackage.TierOutVO;
import io.vilada.higgs.data.web.vo.out.management.newpackage.TierTypeOutVO;
import io.vilada.higgs.data.web.service.elasticsearch.index.agentinfo.AgentInfo;
import io.vilada.higgs.data.web.service.elasticsearch.index.agentinfo.ServerMetaData;
import io.vilada.higgs.data.web.service.elasticsearch.service.info.AgentInfoService;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.transaction.TransactionBuildService;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawei
 * @author nianjun
 * @create 2017-10-10.
 */
@Slf4j
@RestController
@Api(value = "agent controller", description = "所有针对application/tier/instance的元数据操作")
@RequestMapping(value = "/agent", produces = {"application/json;charset=UTF-8"})
public class AgentController {

    @Autowired
    private AgentPackageGenerator agentPackageGenerator;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private TransactionBuildService transactionBuildService;


    @ApiOperation(value = "根据appId,tierId以及type获取下载的探针")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/downloadAgent", method = RequestMethod.GET)
    public void downloadAgent(@RequestParam(value = "applicationId") Long applicationId,
            @RequestParam(value = "type") Byte agentConfigType, @RequestParam(value = "tierId") Long tierId,
            HttpServletResponse response) {
        if (applicationId == null) {
            log.error("application id id is null, failed to download");
            return;
        }
        if (agentConfigType == null) {
            log.error("agent config type is null");
            return;
        }

        if (tierId == null) {
            log.error("tier id name is null");
            return;
        }

        AgentConfigTypeEnum agentConfigTypeEnum = AgentConfigTypeEnum.getAgentConfigTypeEnumByType(agentConfigType);
        if (agentConfigTypeEnum == null) {
            log.warn("there is no agent config type for type id : {}, {}", applicationId, tierId);
            return;
        }

        if (agentConfigTypeEnum.equals(AgentConfigTypeEnum.JAVA)) {
            agentPackageGenerator.generateJavaAgentZipPackage(applicationId, tierId, response);
        } else if (agentConfigTypeEnum.equals(AgentConfigTypeEnum.PHP)) {
            agentPackageGenerator.generatePhpAgentZipPackage(applicationId, tierId, response);
        }
    }

    @ApiOperation(value = "创建Application")
    @RequestMapping(value = "/createApplication", method = RequestMethod.POST)
    public BaseOutVO createApplication(@RequestBody @Valid ApplicationCreateInVO applicationCreateInVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        Agent application = new Agent();
        application.setName(applicationCreateInVO.getName());
        application.setDescription(applicationCreateInVO.getDescription());
        application = agentService.saveApp(application);

        return VOFactory.getSuccessBaseOutVO(application);
    }

    @ApiOperation(value = "创建web探针")
    @RequestMapping(value = "/createWebAgent", method = RequestMethod.POST)
    public BaseOutVO createWebAgent(@RequestBody AppIdInVO appIdInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return VOFactory.getSuccessBaseOutVO(agentService.createWebAgent(appIdInVO.getAppId()));
    }

    @ApiOperation(value = "通过appId获取该application的所有配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/listApplicationConfigurationByApplicationId", method = RequestMethod.GET)
    public ResultVO listApplicationConfigurationByApplicationId(
            @RequestParam(value = "applicationId") Long applicationId) {
        if (applicationId == null) {
            log.error("application id is null, failed to list application configuration");
            return ResultVoFactory.getFailedResultVo(VoMessageEnum.NULL_APPLICATION_ID);
        }

        List<ApplicationConfiguration> applicationConfigurations =
                agentConfigurationService.listAppConfigurationById(applicationId);
        List<ApplicationConfigurationOutVO> applicationConfigurationOutVos = new ArrayList<>();
        if (applicationConfigurations != null && !applicationConfigurations.isEmpty()) {
            for (ApplicationConfiguration applicationConfiguration : applicationConfigurations) {
                ApplicationConfigurationOutVO applicationConfigurationOutVo = new ApplicationConfigurationOutVO();
                BeanUtils.copyProperties(applicationConfiguration, applicationConfigurationOutVo);
                applicationConfigurationOutVo.setId(applicationConfiguration.getId());
                applicationConfigurationOutVo.setApplicationId(applicationConfiguration.getApplicationId());
                applicationConfigurationOutVos.add(applicationConfigurationOutVo);
            }
        }

        return ResultVoFactory.getSuccessResultVo(applicationConfigurationOutVos);
    }

    @RequestMapping(value = "/listAllSystemConfig", method = RequestMethod.GET)
    public BaseOutVO listAllSystemConfig() {
        List<Agent> systemConfigs = agentService.listAllApplication();
        List<SystemConfigOutVO> systemConfigOutVos = new ArrayList<>();
        if (systemConfigs != null && !systemConfigs.isEmpty()) {
            for (Agent agent : systemConfigs) {
                SystemConfigOutVO systemConfigOutVo = new SystemConfigOutVO();
                BeanUtils.copyProperties(agent, systemConfigOutVo);
                systemConfigOutVos.add(systemConfigOutVo);
            }
        }

        return VOFactory.getSuccessBaseOutVO(systemConfigOutVos);
    }

    @ApiOperation(value = "更新application的配置")
    @RequestMapping(value = "/updateApplicationConfiguration", method = RequestMethod.POST)
    public ResultVO updateApplicationConfiguration(
            @RequestBody ApplicationConfigurationPackInVO applicationConfigurationPackInVo) {
        List<ApplicationConfigurationInVO> applicationConfigurationInVos =
                applicationConfigurationPackInVo.getApplicationConfigurationInVos();
        if (applicationConfigurationInVos == null || applicationConfigurationInVos.isEmpty()) {
            log.warn("applicationConfigurationInVos is empty, no need to update");
            ResultVoFactory.getFailedResultVo();
        }

        List<AgentConfiguration> applicationConfigurations = new ArrayList<>();
        for (ApplicationConfigurationInVO applicationConfigurationVo : applicationConfigurationInVos) {
            AgentConfiguration agentConfiguration = new AgentConfiguration();
            BeanUtils.copyProperties(applicationConfigurationVo, agentConfiguration);
            agentConfiguration.setAgentId(applicationConfigurationVo.getApplicationId());
            applicationConfigurations.add(agentConfiguration);
        }
        boolean result = agentConfigurationService.updateAgentConfiguration(applicationConfigurations);

        if (result) {
            return ResultVoFactory.getSuccessResultVo();
        } else {
            return ResultVoFactory.getFailedResultVo();
        }
    }

    @ApiOperation("获取所有Application")
    @RequestMapping(value = "/listAllApplication", method = RequestMethod.GET)
    public ResultVO listAllApplication() {
        List<ApplicationHierarchyListBO> applicationHierarchyListBos = agentService.listApplicationHierarchy();
        List<ApplicationHierarchyListOutVO> applicationHierarchyListOutVos = new ArrayList<>();
        if (applicationHierarchyListBos != null && !applicationHierarchyListBos.isEmpty()) {
            for (ApplicationHierarchyListBO applicationHierarchyListBo : applicationHierarchyListBos) {
                ApplicationHierarchyListOutVO applicationHierarchyListOutVo = new ApplicationHierarchyListOutVO();
                BeanUtils.copyProperties(applicationHierarchyListBo, applicationHierarchyListOutVo);
                applicationHierarchyListOutVo.setId(applicationHierarchyListBo.getId());
                applicationHierarchyListOutVos.add(applicationHierarchyListOutVo);
            }
        }

        ApplicationHierarchyOutVO applicationHierarchyOutVO = new ApplicationHierarchyOutVO();
        applicationHierarchyOutVO.setApplicationHierarchyListOutVos(applicationHierarchyListOutVos);
        applicationHierarchyOutVO.setTotal(agentService.countApp());

        return ResultVoFactory.getSuccessResultVo(applicationHierarchyOutVO);
    }

    @ApiOperation("根据instance的Id与type获取所有配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "type", required = true, dataType = "Byte", paramType = "query")})
    @RequestMapping(value = "/listInstanceConfigurationByInstanceIdAndType", method = RequestMethod.GET)
    public ResultVO listInstanceConfigurationByInstanceIdAndType(@RequestParam(value = "id") String instanceId,
            @RequestParam(value = "type") Byte type) {
        if (Strings.isNullOrEmpty(instanceId)) {
            log.warn("instance id is null, failed to list configuration");
            return ResultVoFactory.getFailedResultVo(VoMessageEnum.NULL_INSTANCE_ID);
        }

        if (type == null) {
            log.warn("instance type is null, failed to list configuration");
            return ResultVoFactory.getFailedResultVo(VoMessageEnum.NULL_INSTANCE_TYPE);
        }

        List<AgentConfiguration> agentConfigurations =
                agentConfigurationService.listInstanceAgentConfiguration(Long.valueOf(instanceId));
        List<InstanceConfigurationOutVO> instanceConfigurationOutVOs = new ArrayList<>();
        if (agentConfigurations != null && !agentConfigurations.isEmpty()) {
            for (AgentConfiguration agentConfiguration : agentConfigurations) {
                InstanceConfigurationOutVO instanceConfigurationOutVo = new InstanceConfigurationOutVO();
                BeanUtils.copyProperties(agentConfiguration, instanceConfigurationOutVo);
                instanceConfigurationOutVo.setId(String.valueOf(agentConfiguration.getId()));
                instanceConfigurationOutVo.setInstanceId(instanceId);
                instanceConfigurationOutVOs.add(instanceConfigurationOutVo);
            }
        }

        return ResultVoFactory.getSuccessResultVo(instanceConfigurationOutVOs);
    }

    @ApiOperation(value = "根据tierId获取所有instance")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tierId", value = "tier id", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/listInstanceByTierId", method = RequestMethod.GET)
    public ResultVO listInstanceByTierId(@RequestParam("tierId") Long tierId) {
        List<Agent> agents = agentService.listAgentByTierId(tierId);
        List<Instance> instances = new ArrayList<>(agents.size());
        for (Agent agent : agents) {
            Instance instance = new Instance();
            BeanUtils.copyProperties(agent, instance);
            instances.add(instance);
        }
        List<InstanceOutVO> instanceOutVos = new ArrayList<>();
        if (instances != null && !instances.isEmpty()) {
            for (Instance instance : instances) {
                InstanceOutVO instanceOutVo = new InstanceOutVO();
                BeanUtils.copyProperties(instance, instanceOutVo);
                instanceOutVo.setId(instance.getId());
                String address = null;
                AgentInfo agentInfo = agentInfoService.findByInstanceId(instance.getId().toString());
                if (agentInfo != null) {
                    String ip = agentInfo.getIp();
                    String port = "";
                    List<ServerMetaData> serverMetaDatas = agentInfo.getServerMetaData();
                    List<Integer> ports = null;
                    if (serverMetaDatas != null && !serverMetaDatas.isEmpty()) {
                        port = String.valueOf(serverMetaDatas.get(0).getPort());
                        ports = new ArrayList<>(serverMetaDatas.size());
                        for(ServerMetaData serverMetaData : serverMetaDatas){
                            ports.add(serverMetaData.getPort());
                        }
                    }
                    address = ip + ":" + port;
                    instanceOutVo.setIps(Arrays.asList(ip.split(",")));
                    instanceOutVo.setPorts(ports);
                }
                instanceOutVo.setAddress(address);
                instanceOutVos.add(instanceOutVo);
            }
        }

        return ResultVoFactory.getSuccessResultVo(instanceOutVos);
    }

    @ApiOperation(value = "更新instance的启用/禁用状态")
    @RequestMapping(value = "/updateInstanceEnabledByInstanceId", method = RequestMethod.POST)
    public ResultVO updateEnabledById(@RequestBody InstanceEnabledUpdateInVO instanceEnabledUpdateInVo) {
        if (instanceEnabledUpdateInVo.getId() == null) {
            log.warn("instance id is null, failed to update enabled by instance id");
            return ResultVoFactory.getFailedResultVo(VoMessageEnum.NULL_INSTANCE_ID);
        }

        boolean result = agentService.updateEnabledById(instanceEnabledUpdateInVo.getId(),
                instanceEnabledUpdateInVo.isEnabled());
        if (result) {
            return ResultVoFactory.getSuccessResultVo();
        } else {
            return ResultVoFactory.getFailedResultVo();
        }
    }

    @ApiOperation(value = "更新instance的名字")
    @RequestMapping(value = "/updateInstanceNameByInstanceId", method = RequestMethod.POST)
    public BaseOutVO updateNameById(@RequestBody @Valid InstanceNameUpdateInVO instanceNameUpdateInVo,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        boolean result = agentService.updateNameById(instanceNameUpdateInVo.getId(), instanceNameUpdateInVo.getName());

        if (result) {
            return VOFactory.getSuccessBaseOutVO();
        } else {
            return VOFactory.getBaseOutVO(-1, "update failed");
        }
    }

    @ApiOperation(value = "更新tier的名字")
    @RequestMapping(value = "/updateTierName", method = RequestMethod.POST)
    public BaseOutVO updateTierName(@RequestBody @Valid TierNameUpdateInVO tierNameUpdateInVOBaseInVO) {
        boolean result =
                agentService.updateTierName(tierNameUpdateInVOBaseInVO.getId(), tierNameUpdateInVOBaseInVO.getName());

        if (result) {
            return VOFactory.getSuccessBaseOutVO();
        } else {
            return VOFactory.getBaseOutVO(-1, "update failed");
        }
    }

    @ApiOperation("更新instance的配置")
    @RequestMapping(value = "/updateInstanceConfiguration", method = RequestMethod.POST)
    public ResultVO updateInstanceConfiguration(
            @RequestBody InstanceConfigurationPackInVO instanceConfigurationPackInVo) {
        List<InstanceConfigurationInVO> instanceConfigurationInVos =
                instanceConfigurationPackInVo.getInstanceConfigurationInVos();
        if (instanceConfigurationInVos == null || instanceConfigurationInVos.isEmpty()) {
            log.warn("there is no instance in instanceConfigurationInVos, no need to update any configuration");
            return ResultVoFactory.getSuccessResultVo();
        }

        List<AgentConfiguration> instanceConfigurations = new ArrayList<>();
        for (InstanceConfigurationInVO instanceConfigurationInVo : instanceConfigurationInVos) {
            AgentConfiguration agentConfiguration = new AgentConfiguration();
            BeanUtils.copyProperties(instanceConfigurationInVo, agentConfiguration);
            agentConfiguration.setAgentId(instanceConfigurationInVo.getInstanceId());
            instanceConfigurations.add(agentConfiguration);
        }

        boolean result = agentConfigurationService.updateAgentConfiguration(instanceConfigurations);

        if (result) {
            return ResultVoFactory.getSuccessResultVo();
        } else {
            return ResultVoFactory.getFailedResultVo();
        }
    }

    @ApiOperation(value = "创建tier")
    @RequestMapping(value = "createTier", method = RequestMethod.POST)
    public BaseOutVO createTier(@RequestBody @Valid TierCreationInVO tierCreationInVo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TierCreationBO tierCreationBO = TierCreationBO.builder().build();
        BeanUtils.copyProperties(tierCreationInVo, tierCreationBO);
        tierCreationBO.setVisible(true);
        Agent tier = agentService.saveTier(tierCreationBO);

        return VOFactory.getSuccessBaseOutVO(tier);
    }

    @ApiOperation("根据applicationId获取所有tier")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "application id", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/listTierByApplicationId", method = RequestMethod.GET)
    public ResultVO listTierByApplicationId(@RequestParam(value = "applicationId") Long applicationId) {
        if (applicationId == null) {
            log.warn("application id is null, failed to list tier");
            return ResultVoFactory.getFailedResultVo(VoMessageEnum.NULL_APPLICATION_ID);
        }

        List<TierOutVO> tierOutVos = new ArrayList<>();

        List<TierHierarchyListBO> tierHierarchyListBos = agentService.listTierHierarchyByApplicationId(applicationId);
        if (tierHierarchyListBos != null && !tierHierarchyListBos.isEmpty()) {
            for (TierHierarchyListBO tierHierarchyListBo : tierHierarchyListBos) {
                TierOutVO tierOutVo = new TierOutVO();
                BeanUtils.copyProperties(tierHierarchyListBo, tierOutVo);
                tierOutVo.setId(tierHierarchyListBo.getId());
                tierOutVos.add(tierOutVo);
            }
        }

        return ResultVoFactory.getSuccessResultVo(tierOutVos);
    }

    @ApiOperation("获取tier的所有type类别")
    @RequestMapping(value = "/listTierType", method = RequestMethod.GET)
    public ResultVO listTierType() {
        TierTypeEnum[] tierTypeEnums = TierTypeEnum.values();
        if (tierTypeEnums == null || tierTypeEnums.length < 1) {
            return ResultVoFactory.getSuccessResultVo();
        }

        List<TierTypeOutVO> tierTypeOutVos = new ArrayList<>(tierTypeEnums.length);

        for (TierTypeEnum tierTypeEnum : tierTypeEnums) {
            TierTypeOutVO tierTypeOutVo =
                    TierTypeOutVO.builder().type(tierTypeEnum.getType()).name(tierTypeEnum.getName()).build();
            tierTypeOutVos.add(tierTypeOutVo);
        }

        return ResultVoFactory.getSuccessResultVo(tierTypeOutVos);
    }

    @ApiOperation(value = "根据appId获取browserInstance")
    @RequestMapping(value = "/getBrowserInstanceByApplicationId", method = RequestMethod.GET)
    public ResultVO getBrowserInstanceByApplicationId(
            @ApiParam(value = "application id", required = true) @RequestParam("applicationId") Long applicationId) {
        if (applicationId == null) {
            log.warn("application id is null, failed to get browser instance");
            return ResultVoFactory.getFailedResultVo(VoMessageEnum.NULL_APPLICATION_ID);
        }

        Agent agent = agentService.getBrowserInstanceByApplicationId(applicationId);
        if (agent == null) {
            return ResultVoFactory.getSuccessResultVo();
        }

        InstanceOutVO instanceOutVo = new InstanceOutVO();
        BeanUtils.copyProperties(agent, instanceOutVo);

        return ResultVoFactory.getSuccessResultVo(instanceOutVo);
    }

    @ApiOperation("根据appId以及type获取所有application的配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId", required = true, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "type", required = true, dataType = "Byte", paramType = "query")})
    @RequestMapping(value = "/listApplicationConfigurationByApplicationIdAndType", method = RequestMethod.GET)
    public ResultVO listApplicationConfigurationByApplicationIdAndType(
            @RequestParam(value = "applicationId") Long applicationId, @RequestParam(value = "type") Byte type) {
        if (applicationId == null) {
            log.warn("application id is null, failed to list application configuration");
            return ResultVoFactory.getFailedResultVo(VoMessageEnum.NULL_APPLICATION_ID);
        }

        if (type == null) {
            log.warn("type is null, failed to list application configuration by applicationId and type");
            return ResultVoFactory.getFailedResultVo(VoMessageEnum.NULL_CONFIGURATION_TYPE);
        }

        List<AgentConfiguration> agentConfigurations = agentConfigurationService
                .listByApplicationIdAndTypes(applicationId, ConfigurationTypeEnum.listCombinationByTypes(type));
        List<ApplicationConfigurationOutVO> applicationConfigurationOutVos = new ArrayList<>();
        if (agentConfigurations != null && !agentConfigurations.isEmpty()) {
            for (AgentConfiguration agentConfiguration : agentConfigurations) {
                ApplicationConfigurationOutVO applicationConfigurationOutVo = new ApplicationConfigurationOutVO();
                BeanUtils.copyProperties(agentConfiguration, applicationConfigurationOutVo);
                applicationConfigurationOutVo.setApplicationId(applicationId);
                applicationConfigurationOutVos.add(applicationConfigurationOutVo);
            }
        }

        return ResultVoFactory.getSuccessResultVo(applicationConfigurationOutVos);
    }

    @ApiOperation(value = "根据application/tier/instance的id删除该元素以及隶属于该元素的子元素(instance不包含子元素)", response = DeletionVO.class)
    @RequestMapping(value = "/deleteById", method = RequestMethod.POST)
    public BaseOutVO deleteById(@RequestBody @Valid DeletionVO deletionVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        boolean result = agentService.deleteById(deletionVO.getId());
        return VOFactory.getSuccessBaseOutVO(result);
    }

    @ApiOperation(value = "更新app的apdex")
    @RequestMapping(value = "/updateApdexTForApp", method = RequestMethod.POST)
    public BaseOutVO updateApdexTForApp(@RequestBody @Valid ApdexForAppVO apdexForAppVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ApdexForAppInBO apdexForAppInBO = new ApdexForAppInBO();
        BeanUtils.copyProperties(apdexForAppVO, apdexForAppInBO);
        agentConfigurationService.updateApdexTForApp(apdexForAppInBO);

        return VOFactory.getSuccessBaseOutVO(apdexForAppVO);
    }

    @ApiOperation(value = "更新instance的apdex")
    @RequestMapping(value = "/updateApdexTForInstance", method = RequestMethod.POST)
    public BaseOutVO updateApdexTForInstance(@RequestBody @Valid ApdexForInstanceVO apdexForAppVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ApdexForInstanceInBO apdexForInstanceInBO = new ApdexForInstanceInBO();
        BeanUtils.copyProperties(apdexForAppVO, apdexForInstanceInBO);
        agentConfigurationService.updateApdexTForInstance(apdexForInstanceInBO);

        return VOFactory.getSuccessBaseOutVO(apdexForAppVO);
    }

    @ApiOperation(value = "更新事务的apdex")
    @RequestMapping(value = "/updateApdexTForTransaction", method = RequestMethod.POST)
    public BaseOutVO updateApdexTForTransaction(@RequestBody @Valid ApdexForTransactionVO apdexForAppVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ApdexForTransactionBO apdexForTransactionBO = new ApdexForTransactionBO();
        BeanUtils.copyProperties(apdexForAppVO, apdexForTransactionBO);
        agentConfigurationService.updateApdexForTransaction(apdexForTransactionBO);

        return VOFactory.getSuccessBaseOutVO(apdexForAppVO);
    }

    @ApiOperation(value = "根据application id获取所有instance的部分数据的以及对应的ApdexT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "applicationId", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/listInstancesWithApdexT", method = RequestMethod.GET)
    public BaseOutVO listInstancesWithApdexT(@RequestParam(value = "appId") Long appId) {
        if (appId == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    "application id can not be null");
        }

        List<ApdexForInstanceOutBO> apdexForInstanceOutBOS = agentConfigurationService.listInstancesWithApdexT(appId);
        List<ApdexForInstanceOutVO> apdexForInstanceOutVOS = new ArrayList<>();
        for (ApdexForInstanceOutBO apdexForInstanceOutBO : apdexForInstanceOutBOS) {
            ApdexForInstanceOutVO apdexForInstanceOutVO = new ApdexForInstanceOutVO();
            BeanUtils.copyProperties(apdexForInstanceOutBO, apdexForInstanceOutVO);
            apdexForInstanceOutVO.setInstanceId(String.valueOf(apdexForInstanceOutBO.getInstanceId()));
            apdexForInstanceOutVO.setTierId(String.valueOf(apdexForInstanceOutBO.getTierId()));
            apdexForInstanceOutVOS.add(apdexForInstanceOutVO);
        }

        return VOFactory.getSuccessBaseOutVO(apdexForInstanceOutVOS);
    }

    @ApiOperation(value = "根据application id获取所有transaction以及对应的ApdexT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "applicationId", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/listTransactionForApdexT", method = RequestMethod.GET)
    public BaseOutVO listTransactionForApdexT(@RequestParam(value = "appId") Long appId) {
        if (appId == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    "application id can not be null");
        }

        ApdexForTransactionVO apdexForTransactionVO = new ApdexForTransactionVO();
        List<ApdexForTransaction> apdexForTransactions = transactionBuildService.listTransactionNameBelongsToApp(appId);
        apdexForTransactionVO.setAppId(appId);
        apdexForTransactionVO.setApdexForTransactions(apdexForTransactions);

        return VOFactory.getSuccessBaseOutVO(apdexForTransactionVO);
    }

    @ApiOperation(value = "根据application id获取app的ApdexT")
    @ApiImplicitParam(paramType = "query", name = "appId", value = "application id", required = true, dataType = "Long")
    @RequestMapping(value = "/getApdexForApp", method = RequestMethod.GET)
    public BaseOutVO getApdexForApp(@RequestParam(value = "appId") Long appId) {
        if (appId == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    "application id can not be null");
        }

        ApdexForAppOutBO apdexForAppOutBO = agentConfigurationService.getApdexForApp(appId);
        return VOFactory.getSuccessBaseOutVO(apdexForAppOutBO);
    }

    @RequestMapping(value = "/batchUpdateConfigurationForTier", method = RequestMethod.POST)
    public BaseOutVO batchUpdateConfigurationForTier(@RequestBody @Valid BatchUpdateForTierVO batchUpdateForTierVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        BatchUpdateForTierBO batchUpdateForTierBO = new BatchUpdateForTierBO();
        BeanUtils.copyProperties(batchUpdateForTierVO, batchUpdateForTierBO);
        agentConfigurationService.batchUpdateForTier(batchUpdateForTierBO);

        return VOFactory.getSuccessBaseOutVO(batchUpdateForTierVO);
    }

    @ApiOperation(value = "根据appId以及配置的key获取对应的配置")
    @RequestMapping(value = "/listAgentConfigurationByAppIdAndKey", method = RequestMethod.GET)
    public BaseOutVO listAgentConfigurationByAppIdAndKey(
            @RequestBody @Valid ApplicationIdAndKeyInVO applicationIdAndKeyInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return VOFactory.getSuccessBaseOutVO(agentConfigurationService
                .listByAppIdAndKey(applicationIdAndKeyInVO.getId(), applicationIdAndKeyInVO.getKey()));
    }

    @ApiOperation(value = "获取前端所需的配置验证规则")
    @RequestMapping(value = "/getAgentConfigurationValidation", method = RequestMethod.GET)
    public BaseOutVO getAgentConfigurationValidation() {

        return VOFactory.getSuccessBaseOutVO(FrontValidationUtils.getValidationContent());
    }

    @ApiOperation(value = "获取app下的所有instance")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "applicationId", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/listInstancesByAppId", method = RequestMethod.GET)
    public BaseOutVO listInstancesByAppId(@RequestParam(value = "appId") Long appId) {
        if (appId == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getMessage());
        }

        return VOFactory.getSuccessBaseOutVO(agentService.listInstancesByAppId(appId));
    }

    @ApiOperation(value = "获取web探针的配置")
    @RequestMapping(value = "/listWebAgentConfiguration", method = RequestMethod.GET)
    public BaseOutVO listWebAgentConfiguration(@RequestParam(value = "appId") Long appId) {
        if (appId == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getMessage());
        }

        List<AgentConfiguration> agentConfigurations =
                agentConfigurationService.listWebAgentConfigurationByAppId(appId);
        List<AgentConfigurationOutVO> agentConfigurationOutVOS = new ArrayList<>();
        if (agentConfigurations != null) {
            for (AgentConfiguration agentConfiguration : agentConfigurations) {
                AgentConfigurationOutVO agentConfigurationOutVO = new AgentConfigurationOutVO();
                BeanUtils.copyProperties(agentConfiguration, agentConfigurationOutVO);
                agentConfigurationOutVO.setId(String.valueOf(agentConfiguration.getId()));
                agentConfigurationOutVO.setAgentId(String.valueOf(agentConfiguration.getAgentId()));
                agentConfigurationOutVOS.add(agentConfigurationOutVO);
            }
        }

        return VOFactory.getSuccessBaseOutVO(agentConfigurationOutVOS);
    }

    @ApiOperation(value = "更新web探针的配置")
    @RequestMapping(value = "/updateWebAgentConfiguration", method = RequestMethod.POST)
    public BaseOutVO updateWebAgentConfiguration(
            @RequestBody @Valid WebAgentConfigurationUpdateInVO webAgentConfigurationUpdateInVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        WebAgentConfigurationUpdateInBO webAgentConfigurationUpdateInBO = new WebAgentConfigurationUpdateInBO();
        BeanUtils.copyProperties(webAgentConfigurationUpdateInVO, webAgentConfigurationUpdateInBO);
        agentConfigurationService.updateWebAgentConfiguration(webAgentConfigurationUpdateInBO);

        return VOFactory.getSuccessBaseOutVO();
    }

    @ApiOperation(value = "根据appId判断该app下是否包含web探针")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "applicationId", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/existWebAgent", method = RequestMethod.GET)
    public BaseOutVO existWebAgent(@RequestParam(value = "appId") Long appId) {
        if (appId == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getMessage());
        }

        return VOFactory.getSuccessBaseOutVO(agentService.existWebAgent(appId));
    }

    @ApiOperation(value = "根据appId获取该app下的web探针的配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "applicationId", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/listWebAgentConfigurationByAppId", method = RequestMethod.GET)
    public BaseOutVO listWebAgentConfigurationByAppId(@RequestParam(value = "appId") Long appId) {
        if (appId == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getMessage());
        }

        return VOFactory.getSuccessBaseOutVO(agentService.listWebAgentConfigurationByAppId(appId));
    }

    @ApiOperation(value = "根据appId获取webAgent的信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "applicationId", required = true, dataType = "Long", paramType = "query")})
    @RequestMapping(value = "/getWebAgentByAppId", method = RequestMethod.GET)
    public BaseOutVO getWebAgentByAppId(@RequestParam(value = "appId") Long appId) {
        if (appId == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getMessage());
        }

        Agent webAgent = agentService.getWebAgentByAppId(appId);
        InstanceOutVO instanceOutVO = new InstanceOutVO();
        BeanUtils.copyProperties(webAgent, instanceOutVO);
        return VOFactory.getSuccessBaseOutVO(instanceOutVO);
    }

}
