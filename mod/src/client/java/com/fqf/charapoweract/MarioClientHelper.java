package com.fqf.charapoweract;

import com.fqf.charapoweract.bapping.AbstractBapInfo;
import com.fqf.charapoweract.bapping.BlockBappingClientUtil;
import com.fqf.charapoweract.util.MarioClientHelperManager;

public class MarioClientHelper implements MarioClientHelperManager.ClientHelper {
	@Override
	public void clientBap(AbstractBapInfo info) {
		BlockBappingClientUtil.clientBap(info);
	}
}
