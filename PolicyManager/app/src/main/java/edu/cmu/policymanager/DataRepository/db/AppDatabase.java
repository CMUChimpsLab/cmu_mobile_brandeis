package edu.cmu.policymanager.DataRepository.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import edu.cmu.policymanager.DataRepository.db.dao.AppInfoDAO;
import edu.cmu.policymanager.DataRepository.db.dao.AskPolicySettingDAO;
import edu.cmu.policymanager.DataRepository.db.dao.MetadataDAO;
import edu.cmu.policymanager.DataRepository.db.dao.OffDevicePolicyDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PermissionInfoDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PolicyProfileDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PolicyProfileSettingDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PolicySettingDAO;
import edu.cmu.policymanager.DataRepository.db.dao.PurposeInfoDAO;
import edu.cmu.policymanager.DataRepository.db.dao.ThirdPartyLibDAO;
import edu.cmu.policymanager.DataRepository.db.model.AppInfo;
import edu.cmu.policymanager.DataRepository.db.model.AskPolicySetting;
import edu.cmu.policymanager.DataRepository.db.model.Metadata;
import edu.cmu.policymanager.DataRepository.db.model.OffDevicePolicyDBModel;
import edu.cmu.policymanager.DataRepository.db.model.PermissionInfo;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfile;
import edu.cmu.policymanager.DataRepository.db.model.PolicyProfileSetting;
import edu.cmu.policymanager.DataRepository.db.model.PolicySetting;
import edu.cmu.policymanager.DataRepository.db.model.PurposeInfo;
import edu.cmu.policymanager.DataRepository.db.model.ThirdPartyLibInfo;

/**
 * Created by shawn on 3/21/2018.
 */

@Database(entities = {AppInfo.class,
                      PermissionInfo.class,
                      PurposeInfo.class,
                      PolicySetting.class,
                      Metadata.class,
                      ThirdPartyLibInfo.class,
                      PolicyProfile.class,
                      PolicyProfileSetting.class,
                      AskPolicySetting.class,
                      OffDevicePolicyDBModel.class},
          exportSchema = false, version = 15)
public abstract class AppDatabase extends RoomDatabase {
    abstract public AppInfoDAO appInfoDao();

    abstract public PermissionInfoDAO permissionInfoDao();

    abstract public PurposeInfoDAO purposeInfoDao();

    abstract public PolicySettingDAO policySettingDao();

    abstract public MetadataDAO metadataDao();

    abstract public ThirdPartyLibDAO thirdPartyLibDao();

    abstract public PolicyProfileSettingDAO policyProfileSettingDAO();

    abstract public PolicyProfileDAO policyProfileDAO();

    abstract public AskPolicySettingDAO askPolicySettingDAO();

    abstract public OffDevicePolicyDAO odpDAO();
}