/*
 * Tencent is pleased to support the open source community by making BK-JOB蓝鲸智云作业平台 available.
 *
 * Copyright (C) 2021 Tencent.  All rights reserved.
 *
 * BK-JOB蓝鲸智云作业平台 is licensed under the MIT License.
 *
 * License for BK-JOB蓝鲸智云作业平台:
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.tencent.bk.job.manage.api.esb.v4;

import com.tencent.bk.audit.annotations.AuditEntry;
import com.tencent.bk.audit.annotations.AuditRequestBody;
import com.tencent.bk.job.common.constant.ErrorCode;
import com.tencent.bk.job.common.esb.metrics.EsbApiTimed;
import com.tencent.bk.job.common.esb.model.v4.EsbV4Response;
import com.tencent.bk.job.common.esb.util.EsbDTOAppScopeMappingHelper;
import com.tencent.bk.job.common.exception.InvalidParamException;
import com.tencent.bk.job.common.iam.constant.ActionId;
import com.tencent.bk.job.common.metrics.CommonMetricNames;
import com.tencent.bk.job.common.service.AppScopeMappingService;
import com.tencent.bk.job.common.util.JobContextUtil;
import com.tencent.bk.job.common.util.check.IllegalCharChecker;
import com.tencent.bk.job.common.util.check.MaxLengthChecker;
import com.tencent.bk.job.common.util.check.NotEmptyChecker;
import com.tencent.bk.job.common.util.check.StringCheckHelper;
import com.tencent.bk.job.common.util.check.TrimChecker;
import com.tencent.bk.job.common.util.check.exception.StringCheckException;
import com.tencent.bk.job.manage.model.dto.task.TaskPlanInfoDTO;
import com.tencent.bk.job.manage.model.esb.v4.req.V4CreateJobPlanRequest;
import com.tencent.bk.job.manage.model.esb.v4.req.V4CreateJobPlanVariableItem;
import com.tencent.bk.job.manage.model.esb.v4.resp.V4CreateJobPlanResp;
import com.tencent.bk.job.manage.model.web.request.TaskPlanCreateUpdateReq;
import com.tencent.bk.job.manage.service.plan.TaskPlanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class OpenApiCreateJobPlanV4ResourceImpl implements OpenApiCreateJobPlanV4Resource {

    private final TaskPlanService planService;
    private final AppScopeMappingService appScopeMappingService;

    public OpenApiCreateJobPlanV4ResourceImpl(TaskPlanService planService,
                                              AppScopeMappingService appScopeMappingService) {
        this.planService = planService;
        this.appScopeMappingService = appScopeMappingService;
    }

    @Override
    @AuditEntry(actionId = ActionId.CREATE_JOB_PLAN)
    @EsbApiTimed(value = CommonMetricNames.ESB_API, extraTags = {"api_name", "v4_create_job_plan"})
    public EsbV4Response<V4CreateJobPlanResp> createJobPlan(
        String username,
        String appCode,
        @AuditRequestBody V4CreateJobPlanRequest request
    ) {
        request.fillAppResourceScope(appScopeMappingService);
        Long appId = request.getAppId();

        TaskPlanCreateUpdateReq createReq = toTaskPlanCreateUpdateReq(request);
        checkPlanName(createReq);

        TaskPlanInfoDTO savedPlan =
            planService.createTaskPlan(JobContextUtil.getUser(), TaskPlanInfoDTO.fromReq(username, appId, createReq));

        V4CreateJobPlanResp data = new V4CreateJobPlanResp();
        EsbDTOAppScopeMappingHelper.fillEsbAppScopeDTOByAppId(appId, data);
        data.setJobPlanId(savedPlan.getId());
        data.setName(savedPlan.getName());
        data.setJobTemplateId(savedPlan.getTemplateId());
        return EsbV4Response.success(data);
    }

    private static TaskPlanCreateUpdateReq toTaskPlanCreateUpdateReq(V4CreateJobPlanRequest request) {
        TaskPlanCreateUpdateReq createReq = new TaskPlanCreateUpdateReq();
        createReq.setTemplateId(request.getJobTemplateId());
        createReq.setName(request.getName());
        createReq.setEnableSteps(
            request.getEnableSteps() == null ? Collections.emptyList() : request.getEnableSteps()
        );
        if (CollectionUtils.isNotEmpty(request.getVariables())) {
            createReq.setVariables(
                request.getVariables().stream()
                    .map(V4CreateJobPlanVariableItem::toTaskVariableVO)
                    .collect(Collectors.toList())
            );
        } else {
            createReq.setVariables(Collections.emptyList());
        }
        return createReq;
    }

    private void checkPlanName(TaskPlanCreateUpdateReq taskPlanCreateUpdateReq) {
        try {
            StringCheckHelper stringCheckHelper = new StringCheckHelper(
                new TrimChecker(),
                new NotEmptyChecker(),
                new IllegalCharChecker(),
                new MaxLengthChecker(60)
            );
            taskPlanCreateUpdateReq.setName(
                stringCheckHelper.checkAndGetResult(taskPlanCreateUpdateReq.getName())
            );
        } catch (StringCheckException e) {
            log.warn("TaskPlan name is invalid:", e);
            throw new InvalidParamException(ErrorCode.ILLEGAL_PARAM);
        }
    }
}
