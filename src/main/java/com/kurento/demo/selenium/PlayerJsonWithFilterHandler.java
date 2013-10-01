/*
 * (C) Copyright 2013 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package com.kurento.demo.selenium;

import com.kurento.kmf.content.ContentException;
import com.kurento.kmf.content.PlayRequest;
import com.kurento.kmf.content.PlayerHandler;
import com.kurento.kmf.content.PlayerService;
import com.kurento.kmf.content.internal.player.PlayRequestImpl;
import com.kurento.kmf.content.jsonrpc.JsonRpcEvent;
import com.kurento.kmf.media.MediaEventListener;
import com.kurento.kmf.media.MediaPipeline;
import com.kurento.kmf.media.MediaPipelineFactory;
import com.kurento.kmf.media.PlayerEndPoint;
import com.kurento.kmf.media.ZBarEvent;
import com.kurento.kmf.media.ZBarFilter;
import com.kurento.kms.api.MediaType;

@PlayerService(name = "PlayerJsonWithFilterHandler", path = "/playerJsonZBar", useControlProtocol = true)
public class PlayerJsonWithFilterHandler implements PlayerHandler {

	@Override
	public void onPlayRequest(final PlayRequest playRequest)
			throws ContentException {
		try {

			MediaPipelineFactory mpf = playRequest.getMediaPipelineFactory();
			MediaPipeline mp = mpf.createMediaPipeline();
			((PlayRequestImpl)playRequest).addForCleanUp(mp);

			PlayerEndPoint player = mp.createUriEndPoint(PlayerEndPoint.class,
					"https://ci.kurento.com/video/barcodes.webm");
			ZBarFilter zBarFilter = mp.createFilter(ZBarFilter.class);
			player.getMediaSrcs(MediaType.VIDEO)
					.iterator()
					.next()
					.connect(
							zBarFilter.getMediaSinks(MediaType.VIDEO)
									.iterator().next());
			playRequest.usePlayer(player);
			playRequest.play(zBarFilter);
			playRequest.setAttribute("eventValue", "");
			zBarFilter.addListener(new MediaEventListener<ZBarEvent>() {
				@Override
				public void onEvent(ZBarEvent event) {
					if (playRequest.getAttribute("eventValue").toString()
							.equals(event.getValue())) {
						return;
					}
					playRequest.setAttribute("eventValue", event.getValue());
					((PlayRequestImpl) playRequest).produceEvents(JsonRpcEvent
							.newEvent(event.getType(), event.getValue()));
				}
			});

		} catch (Throwable t) {
			playRequest.reject(500, t.getMessage());
		}

	}

	@Override
	public void onContentPlayed(PlayRequest playRequest) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContentError(PlayRequest playRequest,
			ContentException exception) {
		// TODO Auto-generated method stub

	}

}
