package wili_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		exclude = {
				org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration.class,
				org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration.class,
				org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration.class
		}
)
public class WiliBeApplication {
	public static void main(String[] args) {
		SpringApplication.run(WiliBeApplication.class, args);
	}
}


/**
 * org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration: AWS EC2 인스턴스 데이터를 로드하는 자동 구성 클래스. 이 클래스를 제외하면 EC2 인스턴스 메타데이터를 사용하는 기능을 비활성화한다.
 * 로컬에서 자꾸 에러가 발생하는 이유가 이 옵션이라 이 클래스를 context에 제외하면 주요 에러를 제거할 수 있다.
 *
 *
 * org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration: AWS CloudFormation 스택 리소스에 대한 액세스를 위한 자동 구성 클래스. 이 클래스를 제외하면 CloudFormation 스택 리소스를 사용하는 기능을 비활성화한다.
 * 이 기능은 yaml의 cloud.aws.stack.auto:false와 같다. 여기서 설정하면, yaml에선 삭제해도 된다.
 *
 *
 * org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration: AWS 리전을 자동으로 감지하는 자동 구성 클래스. 이 클래스를 제외하면 자동 리전 감지 기능을 비활성화할 수 있다.
 * 리전은 S3Config에서 설정해 줄 예정이니 미리 감지하지 않아도 된다. 이 설정을 하면 yaml에서 region.static 설정을 삭제해도 된다.
 */
