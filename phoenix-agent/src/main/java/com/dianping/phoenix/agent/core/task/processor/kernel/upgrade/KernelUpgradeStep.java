package com.dianping.phoenix.agent.core.task.processor.kernel.upgrade;

import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.phoenix.agent.core.task.workflow.AbstractStep;
import com.dianping.phoenix.agent.core.task.workflow.Context;
import com.dianping.phoenix.agent.core.task.workflow.Step;

public class KernelUpgradeStep extends AbstractStep {

	protected KernelUpgradeStep(AbstractStep nextStepWhenSuccess, AbstractStep nextStepWhenFail, int stepSeq) {
		super(nextStepWhenSuccess, nextStepWhenFail, stepSeq);
	}

	private static KernelUpgradeStep FAILED = new KernelUpgradeStep(null, null, 11) {
		@Override
		public int doStep(Context ctx) throws Exception {
			KernelUpgradeContext myCtx = (KernelUpgradeContext) ctx;
			myCtx.setEndStep(FAILED);
			myCtx.setExitCode(Step.CODE_ERROR);
			com.dianping.cat.message.Transaction trans = myCtx.getCatTransaction();
			if (trans != null) {
				trans.setStatus(STATUS_FAIL);
				trans.complete();
			}
			return Step.CODE_ERROR;
		}

		@Override
		public Map<String, String> getLogChunkHeader() {
			Map<String, String> header = super.getLogChunkHeader();
			header.put(HEADER_STATUS, STATUS_FAIL);
			return header;
		}

		@Override
		public String toString() {
			return "FAILED";
		}

	};

	private static KernelUpgradeStep SUCCESS = new KernelUpgradeStep(null, null, 11) {
		@Override
		public int doStep(Context ctx) throws Exception {
			KernelUpgradeContext myCtx = (KernelUpgradeContext) ctx;
			myCtx.setEndStep(SUCCESS);
			myCtx.setExitCode(Step.CODE_OK);
			com.dianping.cat.message.Transaction trans = myCtx.getCatTransaction();
			if (trans != null) {
				trans.setStatus(Message.SUCCESS);
				trans.complete();
			}
			return Step.CODE_OK;
		}

		@Override
		public Map<String, String> getLogChunkHeader() {
			Map<String, String> header = super.getLogChunkHeader();
			header.put(HEADER_STATUS, STATUS_SUCCESS);
			return header;
		}

		@Override
		public String toString() {
			return "SUCCESS";
		}
	};

	private static KernelUpgradeStep ROLLBACK = new KernelUpgradeStep(FAILED, FAILED, 10) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).rollback(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "ROLLBACK";
		}
	};

	private static KernelUpgradeStep COMMIT = new KernelUpgradeStep(SUCCESS, FAILED, 9) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).commit(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "COMMIT";
		}
	};

	private static KernelUpgradeStep CHECK_CONTAINER_STATUS = new KernelUpgradeStep(COMMIT, ROLLBACK, 8) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).checkContainerStatus(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "CHECK_CONTAINER_STATUS";
		}
	};

	private static KernelUpgradeStep START_CONTAINER = new KernelUpgradeStep(CHECK_CONTAINER_STATUS, ROLLBACK, 7) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).startContainer(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "START_CONTAINER";
		}
	};

	private static KernelUpgradeStep UPGRADE_KERNEL = new KernelUpgradeStep(START_CONTAINER, ROLLBACK, 6) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).upgradeKernel(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "UPGRADE_KERNEL";
		}
	};

	private static KernelUpgradeStep STOP_ALL = new KernelUpgradeStep(UPGRADE_KERNEL, ROLLBACK, 5) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).stopAll(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "STOP_ALL";
		}
	};

	private static KernelUpgradeStep GET_KERNEL_WAR = new KernelUpgradeStep(STOP_ALL, ROLLBACK, 4) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).getKernelWar(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "GET_KERNEL_WAR";
		}
	};

	private static KernelUpgradeStep INJECT_PHOENIX_LOADER = new KernelUpgradeStep(GET_KERNEL_WAR, ROLLBACK, 3) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).injectPhoenixLoader(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "INJECT_PHOENIX_LOADER";
		}
	};

	private static KernelUpgradeStep CHECK_ARGUMENT = new KernelUpgradeStep(INJECT_PHOENIX_LOADER, FAILED, 2) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).checkArgument(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "CHECK_ARGUMENT";
		}
	};

	private static KernelUpgradeStep INIT = new KernelUpgradeStep(CHECK_ARGUMENT, FAILED, 1) {
		@Override
		public int doStep(Context ctx) throws Exception {
			com.dianping.cat.message.Transaction trans = generateCatTransaction(ctx, toString());
			int stepCode = getStepProvider(ctx).init(ctx);
			completeTransaction(trans, stepCode);
			return stepCode;
		}

		@Override
		public String toString() {
			return "INIT";
		}
	};

	public static KernelUpgradeStep START = new KernelUpgradeStep(INIT, FAILED, 0) {
		@Override
		public String toString() {
			return "START";
		}
	};

	private static KernelUpgradeStepProvider getStepProvider(Context ctx) {
		return ((KernelUpgradeContext) ctx).getStepProvider();
	}

	@Override
	public int doStep(Context ctx) throws Exception {
		return Step.CODE_OK;
	}

	@Override
	protected int getTotalStep() {
		return 11;
	}

	private static void setParentCatTransaction(Context ctx) {
		try {
			Cat.getManager().getThreadLocalMessageTree()
					.setMessageId(((KernelUpgradeContext) ctx).getCatTransactionId());
		} catch (Exception e) {
			// ignore it
		}
	}

	private static com.dianping.cat.message.Transaction generateCatTransaction(Context ctx, String stepName) {
		setParentCatTransaction(ctx);
		return Cat.getProducer().newTransaction("KernelStep", stepName);
	}

	private static void completeTransaction(com.dianping.cat.message.Transaction trans, int stepCode) {
		trans.setStatus(stepCode == Step.CODE_OK ? Message.SUCCESS : STATUS_FAIL);
		trans.complete();
	}
}
