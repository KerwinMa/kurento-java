/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
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
package org.kurento.test.client;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;
import org.kurento.client.HttpGetEndpoint;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.test.base.BrowserKurentoClientTest;

/**
 * <strong>Description</strong>: WebRTC to HTTP switch. Test KMS is able to
 * dynamically switch many WebRTC flows to a single HTTP endpoint Setup. Two
 * clients WebRTC send-only with audio/video: A,B. One HTTP-EP: H. Switch
 * between following scenarios: A to H, B to H. At least two round.<br/>
 * <strong>Pipeline</strong>:
 * <ul>
 * <li>WebRtcEndpoint (A) -> HttpGetEndpoint</li>
 * <li>WebRtcEndpoint (B) -> HttpGetEndpoint</li>
 * </ul>
 * <strong>Pass criteria</strong>:
 * <ul>
 * <li>Browsers starts before default timeout</li>
 * <li>Color received by HttpPlayer should be green (RGB #008700, video test of
 * Chrome)</li>
 * </ul>
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @since 4.2.3
 */
public class WebRtc2HttpSwitchTest extends BrowserKurentoClientTest {

	private static final int PLAYTIME = 5; // seconds

	@Test
	public void testWebRtc2HttpSwitch() throws Exception {
		// Media Pipeline
		MediaPipeline mp = kurentoClient.createMediaPipeline();
		WebRtcEndpoint webRtcEndpoint1 = new WebRtcEndpoint.Builder(mp).build();
		WebRtcEndpoint webRtcEndpoint2 = new WebRtcEndpoint.Builder(mp).build();
		HttpGetEndpoint httpGetEndpoint = new HttpGetEndpoint.Builder(mp)
				.build();

		BrowserClient.Builder builderWebrtc = new BrowserClient.Builder()
				.browser(Browser.CHROME).client(Client.WEBRTC);
		BrowserClient.Builder builderPlayer = new BrowserClient.Builder()
				.browser(Browser.CHROME).client(Client.PLAYER);
		try (BrowserClient browser1 = builderWebrtc.build();
				BrowserClient browser2 = builderWebrtc.build();
				BrowserClient browser3 = builderPlayer.build()) {

			// WebRTC
			browser1.subscribeEvents("playing");
			browser1.initWebRtc(webRtcEndpoint1, WebRtcChannel.AUDIO_AND_VIDEO,
					WebRtcMode.SEND_ONLY);
			browser2.subscribeEvents("playing");
			browser2.initWebRtc(webRtcEndpoint2, WebRtcChannel.AUDIO_AND_VIDEO,
					WebRtcMode.SEND_ONLY);

			// Round #1: Connecting WebRTC #1 to HttpEnpoint
			webRtcEndpoint1.connect(httpGetEndpoint);
			browser3.consoleLog(ConsoleLogLevel.info,
					"Connecting to WebRTC #1 source");

			browser3.setURL(httpGetEndpoint.getUrl());
			browser3.subscribeEvents("playing");
			browser3.start();
			Assert.assertTrue("Timeout waiting playing event",
					browser3.waitForEvent("playing"));
			Assert.assertTrue(
					"The color of the video should be green (RGB #008700)",
					browser3.similarColor(new Color(0, 135, 0)));

			// Guard time to see stream from WebRTC #1
			Thread.sleep(PLAYTIME * 1000);

			// Round #2: Connecting WebRTC #2 to HttpEnpoint
			webRtcEndpoint2.connect(httpGetEndpoint);
			browser3.consoleLog(ConsoleLogLevel.info,
					"Switching to WebRTC #2 source");

			// Guard time to see stream from WebRTC #2
			Thread.sleep(PLAYTIME * 1000);

			Assert.assertTrue(
					"The color of the video should be green (RGB #008700)",
					browser3.similarColor(new Color(0, 135, 0)));
		}

		// Release Media Pipeline
		mp.release();
	}
}