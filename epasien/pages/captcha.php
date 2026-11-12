<?php
    session_start();
    require_once("../conf/conf.php");
    
    // Header anti-caching browser (Anti-Bypass & Anti-Reuse)
    header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
    header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
    header("Cache-Control: no-store, no-cache, must-revalidate");
    header("Cache-Control: post-check=0, pre-check=0", false);
    header("Pragma: no-cache");
    header("Content-type: image/png");

    $_SESSION["Capcay"]     = "";
    $gbr                    = imagecreate(200, 50);
    
    // Background biru khas Khanza
    imagecolorallocate($gbr, 30, 144, 255);
    $color                  = imagecolorallocate($gbr, 255, 255, 255);
    $font                   = "blackjack.otf"; 
    $ukuran_font            = 20;

    // Tambahkan garis acak (Noise Lines) untuk mengacaukan pembacaan OCR
    for ($l = 0; $l < 5; $l++) {
        $line_color = imagecolorallocate($gbr, rand(80, 200), rand(160, 240), 255);
        imageline($gbr, rand(0, 200), rand(0, 50), rand(0, 200), rand(0, 50), $line_color);
    }

    // Tambahkan bintik acak (Noise Dots) anti-OCR
    for ($d = 0; $d < 80; $d++) {
        $dot_color = imagecolorallocate($gbr, rand(100, 255), rand(180, 255), 255);
        imagesetpixel($gbr, rand(0, 200), rand(0, 50), $dot_color);
    }

    // Render karakter CAPTCHA dengan kemiringan acak bolak-balik (-25 s/d +25) dan jitter posisi
    for ($i = 0; $i <= 5; $i++) {
        $angka              = rand(0, 9);
        $_SESSION["Capcay"].= $angka;
        $kemiringan         = rand(-25, 25);
        $posisi_y           = rand(28, 36);
        imagettftext($gbr, $ukuran_font, $kemiringan, 12 + 28 * $i, $posisi_y, $color, $font, $angka);	
    }
    
    $_SESSION["Capcay"]     = getOne2("select aes_encrypt(".$_SESSION["Capcay"].",'windi')");

    imagepng($gbr); 
    imagedestroy($gbr);
?>