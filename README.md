<img style="width:64px" src="https://i.imgur.com/IIcY60W.png" />

# 保護肩頸大作戰
> Shoulder & Neck Health Challenge

## 介紹影片
<a href="http://www.youtube.com/watch?v=CbMVVM_KWxY"><img title="點擊觀看！" src="https://i.imgur.com/cvYkDEl.png" height="250" ></a>

## 動機
現今社會普遍存在許多低頭族的族群，甚至上班族也會有肩頸問題。因此，我們開發了一款應用程式，能夠提醒高危險族群的人們，在工作或久坐一段時間後，跟著APP運動肩頸，緩解肩頸的肌肉疲勞，養成伸展肩頸肌肉的好習慣，有效緩解肩頸痠痛。 研究顯示[^1][^2]，靜坐姿勢對肩頸造成壓力，且長時間保持同一姿勢最常引起疼痛。因此，許多高危險工作族群包括長時間伏案的上班族、使用電腦螢幕與繪圖設計等相關工作，最容易引起頸部痠痛。
 	
## 介紹
現代人經常長時間低頭使用手機或是盯著電腦螢幕，缺乏保護頸部的習慣，這導致肩頸問題越來越普遍。為了讓使用者能夠經常性地保護自己的肩頸，我們開發了肩頸保健操，並透過這個簡單的鍛鍊方式，鼓勵使用者隨時動動脖子，養成好習慣，並有效預防肩頸痠痛的問題。

##### 肩頸保健操的步驟[^3]
> 1.	背部挺直左手在背後與右手緊扣, 並置於右邊腰部位置 
> 2.	頭部慢慢向正右邊倒並保持30秒
> 3.	頭部慢慢向斜方倒下並同樣保持30秒 
> 4.	可按個人感覺調整頭部方向或下巴位置, 完成後再用另一邊重覆相同動作
 
### 介面設計
<img style="width:500px" src="https://i.imgur.com/6Lt20Jo.png" />

### 流程圖
<img style="width:500px" src="https://i.imgur.com/ghEWPiG.png" />

> 使用訓練好的姿態辨識模型，辨識四種姿態，使用者選擇想要進行的動作後，開始倒數並撥放音樂，須維持動作30秒，當動作不正確時，倒數與音樂將暫停，提示使用者調整姿態。

## 可行性
  本作品之可行性取決於姿態辨識的成效，以下探討我們成功訓練出模型的過程。

#### 辨識方法與選用模型
考慮手機的性能，我們使用由 Google 所推出的`MoveNet-Thunder`此模型具備較高的精確度，專為手機、嵌入式設備所設計，可以快速偵測人體上 17 個特徵點。

`MoveNet`使用熱圖來定位人體關鍵點，模型分為四部並行執行，為了解實際工作的方式，我們以操作順序來敘述[^4]

1.	取得人體的中心熱點，以所有特徵點的算術平均值計算，選擇分數最高得位置。
2.	通過中心對應的像素分割關鍵點回歸輸出該人體的初始關鍵點集。
3.	將熱點圖的每個像素點都乘以與相應特徵點距離成反比的權重，確保不會收到人體以外的關鍵點。
4.	通過搜尋每個特徵點通道中最大熱圖值的座標來選擇特徵點預測的結果，並將局部 2D 偏移量預測加回座標得出更清精確的結果。
<img style="width:500px" src="https://i.imgur.com/OluROxp.png" />

#### 訓練過程:
資料集我們以網路上的影片與組員拍攝的動作建立並區分訓練四個動作分別為

| 姿勢           | 資料集數量    |
| ------------ | ---------- |
| 後仰伸展         | 50/10張    |
| 後頸肌肉伸展       | 50/10張    |
| 右上斜方肌伸展      | 50/10張    |
| 左上斜方肌伸展      | 50/10張    | 

#### 訓練步驟
 	 
1. 將採集訓練集與測試的特徵點轉為`.csv`格是儲存。
2. 資料中共有 17 個特徵點，每個特徵點紀錄score與(x,y)的偏移量。
3. 將姿態特徵移到中心，並攤平座標訓練特特徵向量建立姿態分類器。
   - <img style="width:350px" src="https://i.imgur.com/BsClSKO.png" />
   - <img style="width:350px" src="https://i.imgur.com/vrycIfk.png" />

4. 訓練結果與混淆矩陣。
   - <img style="width:350px" src="https://i.imgur.com/y6y38Ut.png" />

5. 將模型轉換為`tflite`格式縮小大小。
[訓練程式](https://colab.research.google.com/drive/10wGaJf1ts6ldb85SSfY6AK8PFCSEx1zF?usp=sharing)[^6]

## 獨特性
我們透過姿態特徵點辨識，讓使用者能夠確實進行肩頸運動。在查看了 Google Play 商店中類似產品的完成度和使用率後，我們發現這些產品沒有姿勢偵測的功能。

這些產品通常使用動畫或影片的方式讓使用者模仿動作，並在一定時間內完成固定次數的訓練。然而，這種方法容易造成使用者因時間壓力而無法確實完成動作，因此我們相信透過姿勢辨識可以幫助使用者檢測動作是否正確，達到確實地完成運動的目的。透過累積完成次數來增加成就感，進一步提高使用者的滿意度。

綜上所述，我們相信本作品具有獨特性，可與市面上的產品產生差異，並幫助更多人保持良好的肩頸健康。

## License

Shoulder & Neck Health Challenge 使用 MIT License 開放原始碼。

Neck icons created by Freepik - [Flaticon](https://media.flaticon.com/license/license.pdf)

[^1]:詹佑群, 作者, 「[手機使用者在不同姿勢下之肩頸負荷分析](https://ndltd.ncl.edu.tw/cgi-bin/gs32/gsweb.cgi/ccd=NNcOVO/record?r1=2&h1=0)」, 碩士論文, 明志科技大學工業工程與管理系碩士班, 明志科技大學, 2022. 引見於: 2022年12月11日. [線上].
[^2]:林新醫院, 作者, 「[頸部酸痛的預防與保健](http://www.lshosp.com.tw/衛教園地/復健科/頸部酸痛的預防與保健/)」. (引見於 2022年12月11日).
[^3]:Heho健康, 作者, 「[低頭族必備的肩頸伸展操 | Heho健康](https://today.line.me/tw/v2/article/9ZeYXr)」, LINE TODAY.  (引見於 2022年12月11日).
[^4]:「[使用 MoveNet 和 TensorFlow.js 的下一代姿态检测 - 技术分享](https://discuss.tf.wiki/t/topic/1844 )」, tf.wiki 社区, 2021年9月9日. (引見於 2022年12月11日).
[^5]:「[快速舒緩 頸部僵硬疼痛｜早晚一次8分鐘頸部伸展運動｜Stretch Exercise for Neck Pain| Do it every day and night| - YouTube](https://www.youtube.com/watch?v=MwHCEoOSs1E&t=46s)」.  (引見於 2022年12月11日).
[^6]:「[Human Pose Classification with MoveNet and TensorFlow Lite](https://www.tensorflow.org/lite/tutorials/pose_classification )」, TensorFlow. (引見於 2022年12月11日).
