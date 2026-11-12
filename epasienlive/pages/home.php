<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }

    $besok                  = date("Y-m-d", strtotime("+1 day"));
    $thnbesok               = substr($besok,0,4);
    $blnbesok               = substr($besok,5,2);
    $tglbesok               = substr($besok,8,2);
?>
<section id="home" class="slider" data-stellar-background-ratio="0.5">
      <div class="container">
           <div class="row">
                 <div class="owl-carousel owl-theme">
                      <div class="item item-first">
                         
                      </div>
                      <div class="item item-second">
                           <div class="caption">
                                <div class="col-md-offset-1 col-md-10">
                                     <h3>Kami usahakan yang terbaik untuk Anda</h3>
                                     <h1>Jadikan Kami Sahabat Anda</h1>
                                     <a href="#about" class="section-btn btn btn-default btn-gray smoothScroll">Lebih Banyak Tentang Kami</a>
                                </div>
                           </div>
                      </div>
                      <div class="item item-third">
                           <div class="caption">
                                <div class="col-md-offset-1 col-md-10">
                                     <h3>Jangan sampai waktu anda bersama keluarga menjadi terganggu</h3>
                                     <h1>Manfaatkan Kesehatan Anda</h1>
                                     <a href="#news" class="section-btn btn btn-default btn-blue smoothScroll">Lihat Jadwal Dokter</a>
                                </div>
                           </div>
                      </div>
                 </div>
           </div>
      </div>
 </section>

 <!-- ABOUT -->
 <section id="about">
      <div class="container">
           <div class="row">
                <div class="col-md-6 col-sm-6">
                     <div class="about-info">
                          <h2 class="wow fadeInUp" data-wow-delay="0.6s">Sejarah Singkat</h2>
                          <div class="wow fadeInUp" data-wow-delay="0.8s">
                              <p align="justify">Pelayanan RSU Bethesda Serukam telah dimulai sejak tahun 1956 oleh Pdt. John G. Bremen seorang misionari independent diantara suku dayak. Beliau membuka sebuah klinik kesehatan kecil di Gereja Rehobot Sungai Betung yang diberi nama Bethesda. Selanjutnya pada tahun 1965 keluarga dr. Wendell G. Geary dari Misi World Venture-Amerika datang ke Indonesia untuk selanjutnya mengembangkan klinik ini menjadi sebuah Rumah Sakit Pembantu Bethesda di Sungai Betung, mengingat akan kebutuhan pelayanan jangka panjang maka secara bertahap rumah sakit dipindahkan ke Lao-Serukam melalui pertolongan Ir. John Seaders dan alm. Pdt. UT. Simatupang.
Tahun 1971-1981, sebuah mimpi menjadi suatu kenyataan berdiri sebuah Rumah Sakit pedalaman yang terletak di Serukam yang diresmikan oleh Gubernur Kalbar Bapak Kadarusno pada tanggal 23 Februari 1974 dengan kapasitas 70 tempat tidur, pada waktu itu bangunan yang siap digunakan adalah: poliklinik, bangsal rawat inap dan bangunan bedah. Secara bertahap oleh pertolongan Tuhan, tahun demi tahun berikutnya juga dibangun bangsal bedah dan kebidanan, bangsal anak, ICU, gedung administrasi.
Sejak tanggal 12 Agustus 1982 secara resmi status kepemilikan RSUB Serukam diserahkan dari GPIBI ke Yayasan Bethesda Serukam. Selanjutnya sejak tahun 1982 mulai berdatangan dokter-dokter Indonesia antara lain : dr. Sri Sjamsudewi, Sp.Rad. dr. D. Francisca Badudu, Sp. B (K) Onk, dr. Rosita Siregar, dr. Husin Basir, dr. D. J. William Ken dan menyusul beberapa lainnya. Untuk memenuhi kebutuhan dokter spesialis sejak tahun 1984 secara konsisten RSUB Serukam mengirim dokter untuk mengambil program pendidikan dokter spesialis diberbagai tempat baik dalam maupun luar.</p>
                          </div>
                          <figure class="profile wow fadeInUp" data-wow-delay="1s">
                              
                               <figcaption>
                                    <h3>dr. Dewi Citra Puspita, MARS</h3>
                                    <p>Direktur</p>
                               </figcaption>
                          </figure>
                     </div>
                </div>
           </div>
      </div>
 </section>

 <!-- TEAM -->
 <section id="team" data-stellar-background-ratio="1">
      <div class="container">
           <div class="row">
                <div class="col-md-12 col-sm-12">
                     <div class="about-info">
                          <h2 class="section-title wow fadeInUp" data-wow-delay="0.1s"><center>Dokter Kami</center></h2>
                     </div>
                </div>
                <div class="clearfix"></div>
                <?php
                    if(!isset($_SESSION["dokter"])){
                        $delay          = 0.2;
                        $datadokter     = "";
                        $querydokter=bukaquery("select dokter.kd_dokter,left(dokter.nm_dokter,50) as dokter,spesialis.nm_sps from dokter inner join spesialis on dokter.kd_sps=spesialis.kd_sps inner join pegawai on dokter.kd_dokter=pegawai.nik where dokter.status='1' and dokter.kd_dokter <>'-' group by spesialis.nm_sps limit 5");
                        while($rsquerydokter = mysqli_fetch_array($querydokter)) {
                            $datadokter=$datadokter.
                               "<div class='col-md-4 col-sm-6'>
                                    <div class='team-thumb wow fadeInUp' data-wow-delay='".$delay."s'>
                                        
                                          <div class='team-info'>
                                               <h5>$rsquerydokter[1]</h5>
                                               <p>$rsquerydokter[2]</p>
                                               
                                          </div>
                                    </div>
                                    <br/>
                               </div>";
                            $delay=$delay+0.2;
                        }
                        $_SESSION["dokter"]=$datadokter;
                    }

                    echo $_SESSION["dokter"];
                ?>
                <div class="col-md-4 col-sm-6">
                     <div class="wow fadeInUp" data-wow-delay="<?=$delay;?>s">
                         <br/><br/><br/><br/><center><a href='index.php?act=DokterKami' class="btn btn-warning" >Tampilkan Semua Dokter</a></center>
                     </div>
                </div>

           </div>
      </div>
 </section>

 <!-- Jadwal -->
 <section id="news" data-stellar-background-ratio="2.5">
    <div class="container">
         <div class="row">
              <div class="col-md-12 col-sm-12">
                   <div class="section-title wow fadeInUp" data-wow-delay="0.1s">
                        <h2>Jadwal Praktek Dokter</h2>
                   </div>
                   <div class="news-thumb wow fadeInUp" data-wow-delay="0.4s">
                       <form id="carikeyword" name="frmCariJadwal" method="post" action="" enctype=multipart/form-data>
                           <table width="100%" border='0' align="center">
                               <tr class="head">
                                  <td width="20%" align="right"><label for="keyword">Keyword</label></td>
                                  <td width="1%"><label for=":">&nbsp;:&nbsp;</label></td>
                                  <td width="60%"><input name="keyword" type="text" id="keyword" pattern="[a-zA-Z0-9, ./@_]{1,20}" title=" [a-zA-Z0-9, ./@_] (Maksimal 20 karakter) " class="form-control" value="" size="65" maxlength="20" autocomplete="off"/></td>
                                  <td width="19%" align="left">&nbsp;<input name="BtnKeyword" type=submit class="btn btn-warning" value="Cari"></td>
                               </tr>
                           </table>
                       </form>
                       <div id='hasilcari'></div>
                   </div>
              </div>
         </div>
    </div>
 </section>
<div class="col-md-12 col-sm-12" align="center">
<h4>Silahkan Login DORS RSU Bethesda Serukam</h4>
<td width="100%" align="center">
<a href="index.php?act=LoginPasien" class="btn btn-success">LOG IN DORS</a></label><br/><br/>
         </td>   </div> 
 

 <section id="google-map">
      <iframe src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3989.431342411045!2d109.33063391409028!3d0.7980087994178521!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31e36d76ed8f195f%3A0x5eec6360bd6b90ad!2sRumah%20Sakit%20Bethesda%20Serukam!5e0!3m2!1sid!2sid!4v1676620629474!5m2!1sid!2sid" width="100%" height="350" frameborder="0" style="border:0" allowfullscreen="true"></iframe>
 </section> 
 
