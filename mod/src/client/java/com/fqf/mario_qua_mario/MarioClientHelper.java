package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.bapping.AbstractBapInfo;
import com.fqf.mario_qua_mario.bapping.BlockBappingClientUtil;
import com.fqf.mario_qua_mario.util.MarioClientHelperManager;

public class MarioClientHelper implements MarioClientHelperManager.ClientHelper {
	@Override
	public void clientBap(AbstractBapInfo info) {
		BlockBappingClientUtil.clientBap(info);
	}
}
