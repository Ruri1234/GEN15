<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }
?>
<div class="block-header">
    <h2><center>POLI/UNIT YANG KAMI LAYANI</center></h2>
</div>
<div class="row clearfix">
    <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
        <div class="card">
            <div class="body">
                <div class="table-responsive">
                    <table class="table table-bordered table-striped table-hover js-basic-example dataTable">
                        <thead>
                            <tr>
                                <th width="50%"><center>Nama Poliklinik/Unit</center></th>
                                <th width="25%"><center>Registrasi Pasien Baru</center></th>
                                <th width="25%"><center>Registrasi Pasien Lama</center></th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php 
                           $querypoli = bukaquery("select nm_poli,registrasi,registrasilama from poliklinik where status='1' and nm_poli not like '%MARJORIE%' and nm_poli not like '%PROMO%' and nm_poli not like '%APOTEK%' and nm_poli not like '%LABORATORIUM%' and nm_poli not like '%Medical Check Up%' and nm_poli not like '%MCU AKPER%' and nm_poli not in ('APOTEK','MCU AKPER/KARYAWAN','POLI DOTS','POLI GINEKOLOGI','POLI NEONATAL','PROMO LIBUR SEKOLAH','UNIT LABORATORIUM','Medical Check Up','PROMO KEMERDEKAAN','KLINIK MARJORIE') order by nm_poli");
                           while($rsquerypoli = mysqli_fetch_array($querypoli)) {
                               echo "<tr>
                                       <td align='left'>".$rsquerypoli["nm_poli"]."</td>
                                       <td align='center'>".formatDuit($rsquerypoli["registrasi"])."</td>
                                       <td align='center'>".formatDuit($rsquerypoli["registrasilama"])."</td>
                                     </tr>";
                           }
                        ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>