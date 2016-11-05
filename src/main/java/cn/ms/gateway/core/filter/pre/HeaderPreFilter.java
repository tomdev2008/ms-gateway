package cn.ms.gateway.core.filter.pre;

import io.netty.handler.codec.http.HttpHeaders;
import cn.ms.gateway.base.filter.FilterType;
import cn.ms.gateway.base.filter.IFilter;
import cn.ms.gateway.common.Constants;
import cn.ms.gateway.common.annotation.Filter;
import cn.ms.gateway.entity.GatewayREQ;
import cn.ms.gateway.entity.GatewayRES;

/**
 * 请求头参数校验
 * 
 * @author lry
 */
@Filter(value = FilterType.PRE, order = 120)
public class HeaderPreFilter implements IFilter<GatewayREQ, GatewayRES> {

	@Override
	public boolean check(GatewayREQ req, GatewayRES res, Object... args) {
		return true;
	}

	@Override
	public GatewayRES run(GatewayREQ req, GatewayRES res, Object... args) {
		HttpHeaders httpHeaders = req.getRequest().headers();
		if (!httpHeaders.contains(Constants.CHANNELID_KEY)) {//渠道ID参数不存在

		}
		
		if (!httpHeaders.contains(Constants.BIZNO_KEY)) {//业务流水ID参数不存在

		}
		
		if (!httpHeaders.contains(Constants.SYSNO_KEY)) {//系统流水ID参数不存在

		}
		
		return null;
	}

}
