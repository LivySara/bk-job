<!--
 * Tencent is pleased to support the open source community by making BK-JOB蓝鲸智云作业平台 available.
 *
 * Copyright (C) 2021 Tencent.  All rights reserved.
 *
 * BK-JOB蓝鲸智云作业平台 is licensed under the MIT License.
 *
 * License for BK-JOB蓝鲸智云作业平台:
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
-->

<template>
  <div class="task-step-rolling">
    <jb-form-item :label="$t('滚动执行')">
      <span ref="roll">
        <bk-switcher
          theme="primary"
          :value="formData[enabledField]"
          @change="handleRollingEnableChange" />
      </span>
    </jb-form-item>
    <div v-if="formData[enabledField]">
      <!-- 滚动对象类型选择 -->
      <jb-form-item
        ref="rollingType"
        :label="$t('滚动对象')"
        required>
        <bk-radio-group
          class="form-item-content radio-check"
          :value="formData[typeField]"
          @change="handleRollingTypeChange">
          <bk-radio :value="1">
            {{ $t('传输目标') }}
          </bk-radio>
          <bk-radio :value="2">
            {{ $t('源文件') }}
          </bk-radio>
        </bk-radio-group>
      </jb-form-item>
      <!-- 执行模式 -->
      <jb-form-item
        ref="executionMode"
        :label="$t('滚动批次间执行模式')"
        required>
        <bk-radio-group
          class="form-item-content radio-check"
          :value="formData[executionModeField]"
          @change="handleExecutionModeChange">
          <bk-radio :value="1">
            {{ $t('串行执行') }}
          </bk-radio>
          <bk-radio
            :disabled="formData[typeField] === 2"
            :value="2">
            {{ $t('并行执行') }}
          </bk-radio>
        </bk-radio-group>
      </jb-form-item>
      <jb-form-item
        v-show="formData[typeField] === 1"
        ref="expr"
        :label="$t('滚动策略')"
        :property="exprField"
        required
        :rules="rollingExprRule">
        <div class="form-item-content rolling-expr-field">
          <bk-input
            :value="formData[exprField]"
            @change="handleRollingExprChange" />
          <div
            v-if="tips"
            class="strategy-tips">
            {{ tips }}
          </div>
          <div
            v-if="errorMessage"
            class="strategy-error">
            {{ errorMessage }}
          </div>
          <div
            class="rolling-expr-gudie-btn"
            :class="{
              active: isShowGuide,
            }"
            @click="handleShowGuide">
            <icon
              v-bk-tooltips="`${$t('查看使用指引')}`"
              type="help-document-fill" />
          </div>
        </div>
      </jb-form-item>
      <!-- 源文件时显示源文件滚动配置 -->
      <div
        v-if="formData[typeField] === 2"
        class="file-source-section">
        <jb-form-item
          class="batch-form-item"
          :label="$t('分批策略')"
          required>
          <div class="formula-tip">
            {{ $t('单传输目标上的并发任务数 = 单批次最大并发源主机/容器数 * 源单主机/容器最大并发文件数') }}
          </div>
        </jb-form-item>
        <div class="file-source-form">
          <jb-form-item
            ref="maxExecuteObjectNum"
            :label="$t('单批次最大并发源主机/容器数')"
            :label-width="218"
            :property="maxExecuteObjectNumField"
            required
            :rules="maxExecuteObjectNumRule">
            <bk-input
              class="form-item-content"
              :min="1"
              type="number"
              :value="formData[maxExecuteObjectNumField]"
              @change="handleMaxExecuteObjectNumChange" />
          </jb-form-item>
          <jb-form-item
            ref="maxFileNum"
            :label="$t('源单主机/容器最大并发文件数')"
            :label-width="218"
            :property="maxFileNumField"
            required
            :rules="maxFileNumRule">
            <bk-input
              class="form-item-content"
              :min="1"
              type="number"
              :value="formData[maxFileNumField]"
              @change="handleMaxFileNumChange" />
          </jb-form-item>
        </div>
      </div>
      <!-- 滚动机制 - 并行执行时不显示 -->
      <jb-form-item
        v-show="formData[executionModeField] !== 2"
        ref="rollingMode"
        :label="$t('滚动机制')"
        required>
        <bk-select
          class="form-item-content"
          :clearable="false"
          :value="formData[modeField]"
          @change="handleRollingModeChange">
          <bk-option
            :id="1"
            :name="$t('默认（执行失败则暂停）')" />
          <bk-option
            :id="2"
            :name="$t('忽略失败，自动滚动下一批')" />
          <bk-option
            :id="3"
            :name="$t('不自动，每批次都人工确认')" />
        </bk-select>
      </jb-form-item>
      <!-- 并行模式时显示延迟配置 -->
      <div
        v-show="formData[executionModeField] === 2"
        ref="batchStartWait">
        <jb-form-item
          ref="batchStartWaitFixedMs"
          :label="$t('批次间固定延迟（ms）')"
          required>
          <div class="form-item-content">
            <bk-input
              :max="3600000"
              :min="0"
              type="number"
              :value="formData[batchStartWaitFixedMsField]"
              @change="handleBatchStartWaitFixedMsChange" />
          </div>
        </jb-form-item>
        <jb-form-item
          ref="batchStartWaitRandomMinMs"
          :label="$t('批次间随机延迟下限（ms）')"
          required>
          <div class="form-item-content">
            <bk-input
              :max="3600000"
              :min="0"
              type="number"
              :value="formData[batchStartWaitRandomMinMsField]"
              @change="handleBatchStartWaitRandomMinMsChange" />
          </div>
        </jb-form-item>
        <jb-form-item
          ref="batchStartWaitRandomMaxMs"
          :label="$t('批次间随机延迟上限（ms）')"
          required>
          <div class="form-item-content">
            <bk-input
              :max="3600000"
              :min="0"
              type="number"
              :value="formData[batchStartWaitRandomMaxMsField]"
              @change="handleBatchStartWaitRandomMaxMsChange" />
          </div>
        </jb-form-item>
      </div>
    </div>
    <div style="display: none">
      <div ref="tips">
        <img
          :src="bkTips"
          style="float: left; width: 40px; height: 40px;">
        <div style="display: inline-block; padding-left: 4px;">
          <template v-if="$i18n.locale === 'zh-CN'">
            <p>当执行目标数量<span class="strong">比较多</span>时，我们建议开启<span class="strong">“滚动执行”</span>进行分批次灰度处理，</p>
            <p>有效<span class="strong">控制风险</span>。</p>
          </template>
          <template v-else>
            <p>When dealing with a <span class="strong">large number of targets</span>, we recommend enabling <span class="strong">"Rolling execution"</span> to conduct phased gray processing, </p>
            <p>which effectively <span class="strong">controls risks</span></p>
          </template>
        </div>
      </div>
    </div>
    <element-teleport
      v-if="isShowGuide"
      target="#rollingExprGuide">
      <guide @on-close="handleHideGuide" />
    </element-teleport>
  </div>
</template>
<script>
  import Tippy from 'bk-magic-vue/lib/utils/tippy';
  import _ from 'lodash';

  import HostManageService from '@service/host-manage';

  import rollingExprParse from '@utils/rolling-expr-parse';

  import I18n from '@/i18n';

  import Guide from './guide';

  export default {
    name: '',
    components: {
      Guide,
    },
    props: {
      enabledField: {
        type: String,
        required: true,
      },
      typeField: {
        type: String,
        required: true,
      },
      exprField: {
        type: String,
        required: true,
      },
      modeField: {
        type: String,
        required: true,
      },
      executionModeField: {
        type: String,
        required: true,
      },
      maxExecuteObjectNumField: {
        type: String,
        required: true,
      },
      maxFileNumField: {
        type: String,
        required: true,
      },
      batchStartWaitFixedMsField: {
        type: String,
        required: true,
      },
      batchStartWaitRandomMinMsField: {
        type: String,
        required: true,
      },
      batchStartWaitRandomMaxMsField: {
        type: String,
        required: true,
      },
      serverField: {
        type: String,
        required: true,
      },
      formData: {
        type: Object,
        default: () => ({}),
      },
    },
    data() {
      return {
        isShowGuide: false,
        tips: '',
        errorMessage: '',
      };
    },
    computed: {
      /**
       * @desc 滚动策略验证规则，不需要滚动执行时不进行验证
       * @returns { Array }
       */
      rollingExprRule() {
        // 只有当滚动对象类型为传输目标时才验证
        if (!this.formData[this.enabledField] || this.formData[this.typeField] !== 1) {
          return [];
        }
        return [
          {
            validator: (value) => {
              this.errorMessage = '';
              return Boolean(value);
            },
            message: I18n.t('滚动策略必填'),
            trigger: 'blur',
          },
          {
            validator: (value) => {
              this.errorMessage = '';
              try {
                rollingExprParse(value);
                return true;
              } catch {
                return false;
              }
            },
            message: I18n.t('滚动策略格式不正确'),
            trigger: 'blur',
          },
        ];
      },
      /**
       * @desc 单批次最大并发源主机/容器数校验规则
       * @returns { Array }
       */
      maxExecuteObjectNumRule() {
        if (!this.formData[this.enabledField] || this.formData[this.typeField] !== 2) {
          return [];
        }
        return [
          {
            validator: (value) => {
              return Number(value) > 0;
            },
            message: I18n.t('单批次最大并发源主机/容器数必填'),
            trigger: 'blur',
          },
        ];
      },
      /**
       * @desc 源单主机/容器最大并发文件数校验规则
       * @returns { Array }
       */
      maxFileNumRule() {
        if (!this.formData[this.enabledField] || this.formData[this.typeField] !== 2) {
          return [];
        }
        return [
          {
            validator: (value) => {
              return Number(value) > 0;
            },
            message: I18n.t('源单主机/容器最大并发文件数必填'),
            trigger: 'blur',
          },
        ];
      },
    },
    watch: {
      formData: {
        handler(formData) {
          if (formData[this.typeField] === 1) {
            this.validatorExpr(formData[this.exprField]);
          }
          setTimeout(() => {
            this.showTips();
          });
        },
        immediate: true,
        deep: true,
      },
    },
    created() {
      this.bkTips = window.__loadAssetsUrl__('/static/images/bk-tips.png');
    },
    beforeDestroy() {
      if (this.popperInstance) {
        this.popperInstance.hide();
        this.popperInstance.destroy();
      }
      this.popperInstance = undefined;
    },
    mounted() {
      // 因为元素位置的变化导致 tips错位
      const handleBodyClick = () => {
        this.popperInstance && this.popperInstance.state.isShown && this.popperInstance.show();
      };
      document.body.addEventListener('click', handleBodyClick);
      this.$once('hook:beforeDestroy', () => {
        document.body.removeEventListener('click', handleBodyClick);
      });
    },
    methods: {
      showTips() {
        if (this.formData[this.serverField].isEmpty) {
          this.popperInstance && this.popperInstance.hide();
          return;
        }
        if (!this.popperInstance) {
          this.popperInstance = Tippy(this.$refs.roll, {
            arrow: true,
            placement: 'right',
            trigger: 'manual',
            theme: 'light roll-execute-count-tips',
            interactive: true,
            hideOnClick: false,
            animation: 'slide-toggle',
            lazy: false,
            size: 'small',
            boundary: 'window',
            distance: 20,
            zIndex: 990,
          });
          this.popperInstance.setContent(this.$refs.tips);
        }
        const {
          dynamicGroupList = [],
          hostList = [],
          nodeList = [],
          containerList = [],
        } = this.formData[this.serverField].executeObjectsInfo;

        // 选中的主机或者容器大于 100 时直接显示
        if (hostList.length >= 100 || containerList.length >= 100) {
          setTimeout(() => {
            this.popperInstance.show();
          }, 1000);
          return;
        }

        // 容器执行——但是容器数小于 100 不提示
        if (containerList.length > 0 && containerList.length < 100) {
          this.popperInstance.hide();
          return;
        }

        // 主机执行——动态分组和动态拓扑为空并且主机数小于 100 不提示
        if (dynamicGroupList.length < 1 && nodeList.length < 1 && hostList.length < 100) {
          this.popperInstance.hide();
        }

        // 合并查询主机、动态拓扑、动态分组主机数
        HostManageService.fetchHostStatistics({
          dynamicGroupList,
          hostList,
          nodeList,
        }).then((data) => {
          // 异步请求返回时组件可能被销毁了
          if (!this.popperInstance) {
            return;
          }
          if (data.totalCount < 100) {
            this.popperInstance.hide();
            return;
          }
          this.popperInstance.show();
        });
      },
      /**
       * @desc 验证滚动规则
       * @param { String } expr
       */
      validatorExpr(expr) {
        try {
          this.errorMessage = '';
          this.tips = rollingExprParse(expr);
        } catch (error) {
          this.tips = '';
          this.errorMessage = error.message;
        }
      },
      /**
       * @desc 是否启用滚动
       * @param { Boolean } enabled
       */
      handleRollingEnableChange(enabled) {
        this.$emit('on-change', this.enabledField, enabled);
        if (!enabled) {
          this.tips = '';
          this.errorMessage = '';
          this.$emit('on-reset', {
            [this.typeField]: 1,
            [this.exprField]: '',
            [this.modeField]: 1,
            [this.executionModeField]: 1,
            [this.maxExecuteObjectNumField]: null,
            [this.maxFileNumField]: null,
            [this.batchStartWaitFixedMsField]: null,
            [this.batchStartWaitRandomMinMsField]: null,
            [this.batchStartWaitRandomMaxMsField]: null,
          });
        }
        // 默认配置
        this.$emit('on-reset', {
          [this.typeField]: 1,
          [this.exprField]: '10%',
          [this.modeField]: 1,
          [this.executionModeField]: 1,
        });
        this.$nextTick(() => {
          if (this.formData[this.enabledField]) {
            this.$refs.rollingType.$el.scrollIntoView();
          }
        });
      },
      /**
       * @desc 滚动对象类型更新
       * @param { Number } rollingType
       */
      handleRollingTypeChange(rollingType) {
        this.$emit('on-change', this.typeField, rollingType);
        // 源文件不支持并行执行，自动重置为串行执行
        if (rollingType === 2 && this.formData[this.executionModeField] === 2) {
          this.$emit('on-change', this.executionModeField, 1);
        }
      },
      /**
       * @desc 滚动策略更新
       * @param { String } expr 滚动表达式
       */
      handleRollingExprChange: _.debounce(function (expr) {
        this.$refs.expr && this.$refs.expr.clearValidator();
        this.validatorExpr(expr);
        this.$emit('on-change', this.exprField, expr);
      }, 20),
      /**
       * @desc 源文件滚动配置更新
       * @param { Number } value
       * @param { String } field 字段名
       */
      handleMaxExecuteObjectNumChange(value) {
        this.$emit('on-change', this.maxExecuteObjectNumField, value);
      },
      handleMaxFileNumChange(value) {
        this.$emit('on-change', this.maxFileNumField, value);
      },
      /**
       * @desc 滚动机制更新
       * @param { Number } rollingMode
       */
      handleRollingModeChange(rollingMode) {
        this.$emit('on-change', this.modeField, rollingMode);
      },
      /**
       * @desc 执行模式更新
       * @param { Number } executionMode
       */
      handleExecutionModeChange(executionMode) {
        this.$emit('on-change', this.executionModeField, executionMode);
        if (executionMode === 2) {
          // 切换到并行模式时，默认设置延迟值
          this.$emit('on-reset', {
            [this.batchStartWaitFixedMsField]: 5000,
            [this.batchStartWaitRandomMinMsField]: 0,
            [this.batchStartWaitRandomMaxMsField]: 0,
          });
          this.$nextTick(() => {
            this.$refs.batchStartWait.scrollIntoView();
          });
        }
      },
      /**
       * @desc 批次间固定延迟更新
       * @param { Number } value
       */
      handleBatchStartWaitFixedMsChange(value) {
        this.$emit('on-change', this.batchStartWaitFixedMsField, value);
      },
      /**
       * @desc 批次间随机延迟下限更新
       * @param { Number } value
       */
      handleBatchStartWaitRandomMinMsChange(value) {
        this.$emit('on-change', this.batchStartWaitRandomMinMsField, value);
      },
      /**
       * @desc 批次间随机延迟上限更新
       * @param { Number } value
       */
      handleBatchStartWaitRandomMaxMsChange(value) {
        this.$emit('on-change', this.batchStartWaitRandomMaxMsField, value);
      },
      handleShowGuide() {
        this.isShowGuide = !this.isShowGuide;
      },
      handleHideGuide() {
        this.isShowGuide = false;
      },
    },
  };
</script>
<style lang="postcss">
  .task-step-rolling {
    display: block;

    .rolling-expr-field {
      position: relative;

      .form-error-tip {
        display: none;
      }

      .rolling-expr-gudie-btn {
        position: absolute;
        top: 0;
        right: -22px;
        font-size: 14px;
        color: #979ba5;
        cursor: pointer;

        &:hover,
        &.active {
          color: #3a84ff;
        }
      }
    }

    .strategy-tips {
      margin-top: 4px;
      font-size: 12px;
      line-height: 16px;
      color: #979ba5;
    }

    .strategy-error {
      margin-top: 4px;
      font-size: 12px;
      line-height: 18px;
      color: #ea3636;
    }

    .radio-check {
      display: flex;
      align-items: center;
      height: 32px;
      gap: 32px;
    }
  }

  .file-source-section {
    width: 100%;
    .batch-form-item.bk-form-item {
      margin-bottom: 12px;
      .formula-tip {
        font-size: 12px;
        color: #979ba5;
      }
    }

    .file-source-form {
      background-color: #fafafa;
      padding: 16px 12px;
      border-radius: 2px;
      margin-bottom: 12px;

      .jb-form-item {
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  .roll-execute-count-tips-theme{
    &.tippy-tooltip{
      padding: 3px 7px;
      line-height: 20px;
      border-radius: 23px;

      .tippy-arrow {
        left: -2px !important;
      }

      .strong{
        padding: 0 2px;
        color: #FF9C01;
      }
    }
  }
</style>
