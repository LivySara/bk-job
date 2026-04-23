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

package com.tencent.bk.job.manage.model.esb.v4.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.bk.job.common.model.vo.TaskTargetVO;
import com.tencent.bk.job.manage.model.web.vo.task.TaskVariableVO;
import lombok.Getter;
import lombok.Setter;

/**
 * 与 Web 层 {@link TaskVariableVO} 语义一致，用于 OpenAPI V4 创建执行方案时传递变量默认值覆盖等信息。
 */
@Getter
@Setter
public class V4CreateJobPlanVariableItem {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("default_value")
    private String defaultValue;

    @JsonProperty("default_target_value")
    private TaskTargetVO defaultTargetValue;

    @JsonProperty("value")
    private String value;

    @JsonProperty("target_value")
    private TaskTargetVO targetValue;

    @JsonProperty("description")
    private String description;

    @JsonProperty("changeable")
    private Integer changeable;

    @JsonProperty("required")
    private Integer required;

    @JsonProperty("delete")
    private Integer delete;

    public TaskVariableVO toTaskVariableVO() {
        TaskVariableVO vo = new TaskVariableVO();
        vo.setId(id);
        vo.setName(name);
        vo.setType(type);
        vo.setDefaultValue(defaultValue);
        vo.setDefaultTargetValue(defaultTargetValue);
        vo.setValue(value);
        vo.setTargetValue(targetValue);
        vo.setDescription(description);
        vo.setChangeable(changeable);
        vo.setRequired(required);
        vo.setDelete(delete);
        return vo;
    }
}
