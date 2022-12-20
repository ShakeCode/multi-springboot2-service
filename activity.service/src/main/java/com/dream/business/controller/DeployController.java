package com.dream.business.controller;

import com.dream.business.model.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;


/**
 * The type Deploy controller.
 */
@RestController
@Api(tags = "部署流程、删除流程")
public class DeployController {

    private final RepositoryService repositoryService;

    /**
     * Instantiates a new Deploy controller.
     * @param repositoryService the repository service
     */
    public DeployController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /**
     * Deploy result vo.
     * @param bpmnName the bpmn name
     * @return the result vo
     */
    @PostMapping(path = "deploy")
    @ApiOperation(value = "根据bpmnName部署流程", notes = "根据bpmnName部署流程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bpmnName", value = "设计的流程图名称", dataType = "String", paramType = "query", example = "myProcess")
    })
    public ResultVO deploy(@RequestParam("bpmnName") String bpmnName) {

        ResultVO resultVO = new ResultVO();
        //创建一个部署对象
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name("请假流程");
        Deployment deployment = null;
        try {
            deployment = deploymentBuilder
                    .addClasspathResource("processes/" + bpmnName + ".bpmn")
                    .addClasspathResource("processes/" + bpmnName + ".png")
                    .deploy();
        } catch (Exception e) {
            resultVO = ResultVO.fail("部署失败:" + e.getMessage());
            e.printStackTrace();
        }

        if (deployment != null) {
            Map<String, String> result = new HashMap<>(2);
            result.put("deployID", deployment.getId());
            result.put("deployName", deployment.getName());
            resultVO = ResultVO.success("部署成功", result);
        }
        return resultVO;
    }

    /**
     * Deploy zip result vo.
     * @param zipName the zip name
     * @return the result vo
     */
    @PostMapping(path = "deployZIP")
    @ApiOperation(value = "根据ZIP压缩包部署流程", notes = "根据ZIP压缩包部署流程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zipName", value = "设计的流程图和图片的压缩包名称", dataType = "String", paramType = "query", example = "myProcess")
    })
    public ResultVO deployZIP(@RequestParam(value = "zipName", defaultValue = "leaveProcess", required = false) String zipName) {
        ResultVO resultVO = new ResultVO();
        Deployment deployment = null;
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(MessageFormat.format("processes/{0}.zip", zipName));
             ZipInputStream zipInputStream = new ZipInputStream(in)) {
            deployment = repositoryService.createDeployment()
                    .name("请假流程2")
                    //指定zip格式的文件完成部署
                    .addZipInputStream(zipInputStream)
                    .deploy();//完成部署
        } catch (Exception e) {
            resultVO = ResultVO.fail("部署失败：" + e.getMessage());
            e.printStackTrace();
        }
        if (deployment != null) {
            Map<String, String> result = new HashMap<>(2);
            result.put("deployID", deployment.getId());
            result.put("deployName", deployment.getName());
            resultVO = ResultVO.success("部署成功", result);
        }
        return resultVO;
    }

    /**
     * Delete process result vo.
     * @param deploymentId the deployment id
     * @return the result vo
     */
    @PostMapping(path = "deleteProcess")
    @ApiOperation(value = "根据部署ID删除流程", notes = "根据部署ID删除流程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deploymentId", value = "部署ID", dataType = "String", paramType = "query", example = "")
    })
    public ResultVO deleteProcess(@RequestParam("deploymentId") String deploymentId) {
        ResultVO resultVO = new ResultVO();
        /**不带级联的删除：只能删除没有启动的流程，如果流程启动，就会抛出异常*/
        try {
            repositoryService.deleteDeployment(deploymentId);
        } catch (Exception e) {
            resultVO = ResultVO.fail("删除失败：" + e.getMessage());
            // TODO 上线时删除
            e.printStackTrace();
        }

        /**级联删除：不管流程是否启动，都能可以删除（emmm大概是一锅端）*/
//        repositoryService.deleteDeployment(deploymentId, true);
        resultVO = ResultVO.success("删除成功", null);
        return resultVO;
    }
}
