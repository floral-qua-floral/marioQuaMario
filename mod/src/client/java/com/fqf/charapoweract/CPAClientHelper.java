package com.fqf.charapoweract;

import com.fqf.charapoweract.bapping.AbstractBapInfo;
import com.fqf.charapoweract.bapping.BlockBappingClientUtil;
import com.fqf.charapoweract.util.MarioClientHelperManager;

public class CPAClientHelper implements MarioClientHelperManager.ClientHelper {
	@Override
	public void clientBap(AbstractBapInfo info) {
		BlockBappingClientUtil.clientBap(info);
	}
}
