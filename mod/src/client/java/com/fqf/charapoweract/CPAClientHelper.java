package com.fqf.charapoweract;

import com.fqf.charapoweract.bapping.AbstractBapInfo;
import com.fqf.charapoweract.bapping.BlockBappingClientUtil;
import com.fqf.charapoweract.util.CPAClientHelperManager;

public class CPAClientHelper implements CPAClientHelperManager.ClientHelper {
	@Override
	public void clientBap(AbstractBapInfo info) {
		BlockBappingClientUtil.clientBap(info);
	}
}
